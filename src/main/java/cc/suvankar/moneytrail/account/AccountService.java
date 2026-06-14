package cc.suvankar.moneytrail.account;

import cc.suvankar.moneytrail.account.dto.AccountRequest;
import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.contact.ContactService;
import cc.suvankar.moneytrail.exception.*;
import cc.suvankar.moneytrail.transaction.TransactionRepository;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final ContactService contactService;
  private final UserService userService;
  private final TransactionRepository transactionRepository;

  public AccountService(
      AccountRepository accountRepository,
      ContactService contactService,
      UserService userService,
      TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.contactService = contactService;
    this.userService = userService;
    this.transactionRepository = transactionRepository;
  }

  public List<AccountResponse> getAccountsByUserId(@NonNull UUID userId) {
    var accounts = accountRepository.findByUserIdAndVirtualFalse(userId);

    return accounts.stream().map(AccountResponse::from).toList();
  }

  public List<AccountResponse> getVirtualAccountsByCurrency(@NonNull UUID userId, String currency) {
    return accountRepository
        .findByUserIdAndCurrencyAndVirtual(userId, getCurrencyCode(currency), true)
        .stream()
        .map(AccountResponse::from)
        .toList();
  }

  @Transactional
  public AccountResponse createAccount(
      @NonNull UUID userId, @NonNull AccountRequest accountRequest) {
    User user;
    try {
      user = userService.getUserById(userId);
    } catch (ResourceNotFoundException ex) {
      throw new InvalidCredentialsException("Invalid credential");
    }

    // Check account type ASSET/LIABILITY/INVESTMENT
    // - create matching Opening Balance Account / Income Account / Expense Account if it does not
    // exist
    switch (accountRequest.getAccountType()) {
      case ASSET -> {
        createVirtualAccount(user, accountRequest, AccountType.OPENING_BALANCE_EQUITY);
        createVirtualAccount(user, accountRequest, AccountType.INCOME);
        createVirtualAccount(user, accountRequest, AccountType.EXPENSE);
      }
      case LIABILITY -> {
        createVirtualAccount(user, accountRequest, AccountType.OPENING_BALANCE_EQUITY);
        createVirtualAccount(user, accountRequest, AccountType.EXPENSE);
      }
      case INVESTMENT ->
          createVirtualAccount(user, accountRequest, AccountType.OPENING_BALANCE_EQUITY);
      default -> {
        throw new AccountCreationForInvalidAccountTypeException(
            accountRequest.getAccountType().name());
      }
    }

    Account account =
        createAccountObject(
            user,
            accountRequest.getCurrency(),
            accountRequest.getName(),
            accountRequest.getAccountType(),
            false);

    if (accountRequest.getAccountType() == AccountType.RECEIVABLE
        || accountRequest.getAccountType() == AccountType.PAYABLE) {
      if (accountRequest.getContactId() == null) {
        throw new BadRequestException("Contact ID missing.");
      }
      var contact = contactService.getContact(user.getId(), accountRequest.getContactId());
      account.setContact(contact);
    }

    accountRepository.save(account);

    var response = AccountResponse.from(account);

    log.info(
        "Created new account {}, {} of type {}",
        response.getAccountId(),
        response.getName(),
        response.getAccountType());

    return response;
  }

  private void createVirtualAccount(
      @NonNull User user, @NonNull AccountRequest accountRequest, AccountType type) {
    Optional<Account> foundAccountOptional =
        accountRepository.findByUserIdAndTypeAndCurrency(
            user.getId(), type, CurrencyCode.valueOf(accountRequest.getCurrency()));

    if (foundAccountOptional.isPresent()) {
      log.info(
          "{} account for user {} and currency {} is already present.",
          type.name(),
          user.getId(),
          accountRequest.getCurrency());
    } else {
      log.info(
          "Creating an {} account for user {} and currency {} ",
          type.name(),
          user.getId(),
          accountRequest.getCurrency());

      Account virtualAccount =
          createAccountObject(
              user,
              accountRequest.getCurrency(),
              type.name() + " - " + accountRequest.getCurrency(),
              type,
              true);

      accountRepository.save(virtualAccount);
    }
  }

  private CurrencyCode getCurrencyCode(String currency) {
    CurrencyCode currencyCode = CurrencyCode.sanitizeCurrency(currency);

    if (currencyCode == null) {
      throw new IllegalCurrencyCodeException("'" + currency + "'' is not a valid currency code.");
    }

    return currencyCode;
  }

  private Account createAccountObject(
      User user, String currency, String name, AccountType type, boolean isVirtual) {
    Account account = new Account();
    account.setUser(user);
    account.setCurrency(getCurrencyCode(currency));
    account.setName(name);
    account.setType(type);
    account.setVirtual(isVirtual);

    return account;
  }

  @Transactional(readOnly = true)
  public AccountResponse getAccount(@NonNull UUID userId, @NonNull UUID accountId) {
    var account =
        accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::forAccount);

    if (account.getUser().getId().equals(userId)) {
      log.info("Found account {} for user {}", accountId, userId);
      return AccountResponse.from(account);
    }

    log.info("Account {} does not belong to user {}", accountId, userId);

    throw ResourceNotFoundException.forAccount();
  }

  @Transactional
  public AccountResponse updateAccount(
      @NonNull UUID userId, @NonNull UUID accountId, @NonNull AccountRequest accountRequest) {
    var account =
        accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::forAccount);

    if (!account.getUser().getId().equals(userId)) {
      throw ResourceNotFoundException.forAccount();
    }

    // If account is virtual - throw exception
    if (account.isVirtual()) {
      throw new AccountModificationNotAllowedException(accountId.toString());
    }

    // Update fields
    if (accountRequest.getAccountType() == AccountType.RECEIVABLE
        || accountRequest.getAccountType() == AccountType.PAYABLE) {
      if (accountRequest.getContactId() == null) {
        throw new BadRequestException("Contact ID missing.");
      }
      var contact = contactService.getContact(userId, accountRequest.getContactId());
      account.setContact(contact);
    }
    account.setType(accountRequest.getAccountType());

    // Update the currency only if there is no associated xn
    // else throw error
    if (!getCurrencyCode(accountRequest.getCurrency()).equals(account.getCurrency())) {
      if (!transactionRepository.existsByFromAccountOrToAccount(account, account)) {
        account.setCurrency(getCurrencyCode(accountRequest.getCurrency()));
      } else {
        throw new AccountHasTransactionAssociatedException("Account: " + account.getId());
      }
    }

    account.setName(accountRequest.getName());

    accountRepository.save(account);

    log.info("Updated account {}", accountId);

    return AccountResponse.from(account);
  }

  @Transactional
  public void deleteAccount(@NonNull UUID userId, @NonNull UUID accountId) {
    var account =
        accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::forAccount);

    if (!account.getUser().getId().equals(userId)) {
      throw ResourceNotFoundException.forAccount();
    }

    // If account is virtual - throw exception
    if (account.isVirtual()) {
      throw new AccountModificationNotAllowedException(accountId.toString());
    }

    // Soft delete the account
    account.setAccountStatus(AccountStatus.DELETED);
    accountRepository.save(account);

    log.info("Account {} deleted for user {}", accountId, userId);
  }

  @Transactional(readOnly = true)
  public Account findAccountByUserIdAndAccountId(@NonNull UUID userId, @NonNull UUID accountId) {
    return accountRepository
        .findByUserIdAndId(userId, accountId)
        .orElseThrow(ResourceNotFoundException::forAccount);
  }
}
