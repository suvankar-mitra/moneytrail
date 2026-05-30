package cc.suvankar.moneytrail.account;

import cc.suvankar.moneytrail.account.dto.AccountRequest;
import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.contact.ContactService;
import cc.suvankar.moneytrail.exception.AccountHasTransactionAssociatedException;
import cc.suvankar.moneytrail.exception.BadRequestException;
import cc.suvankar.moneytrail.exception.InvalidCredentialsException;
import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
import cc.suvankar.moneytrail.transaction.TransactionRepository;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserService;
import java.util.List;
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
    var accounts = accountRepository.findByUserId(userId);

    return accounts.stream().map(AccountResponse::from).toList();
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

    Account account = new Account();
    account.setUser(user);
    account.setCurrency(accountRequest.getCurrency());
    account.setName(accountRequest.getName());
    account.setType(accountRequest.getAccountType());
    account.setVirtual(accountRequest.isVirtual());

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
    if (!transactionRepository.existsByFromAccountOrToAccount(account, account)) {
      account.setCurrency(accountRequest.getCurrency());
    } else {
      throw new AccountHasTransactionAssociatedException("Account: " + account.getId());
    }

    account.setVirtual(accountRequest.isVirtual());
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

    // Soft delete the account
    account.setAccountStatus(AccountStatus.DELETED);
    accountRepository.save(account);

    log.info("Account {} deleted for user {}", accountId, userId);
  }
}
