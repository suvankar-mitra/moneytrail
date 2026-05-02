package cc.suvankar.moneytrail.account;

import cc.suvankar.moneytrail.account.dto.AccountRequest;
import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.contact.ContactService;
import cc.suvankar.moneytrail.exception.BadRequestException;
import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
import cc.suvankar.moneytrail.user.UserService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final ContactService contactService;
  private final UserService userService;

  public AccountService(
      AccountRepository accountRepository, ContactService contactService, UserService userService) {
    this.accountRepository = accountRepository;
    this.contactService = contactService;
    this.userService = userService;
  }

  private AccountResponse toAccountResponse(Account account) {
    AccountResponse response = new AccountResponse();
    response.setAccountId(account.getId());
    response.setCreatedAt(account.getCreatedAt());
    if (account.getType() == AccountType.RECEIVABLE || account.getType() == AccountType.PAYABLE) {
      if (account.getContact() == null) {
        throw ResourceNotFoundException.forContact();
      }
      response.setContactId(account.getContact().getId());
    }
    response.setAccountType(account.getType());
    response.setCurrency(account.getCurrency());
    response.setVirtual(account.isVirtual());
    response.setUpdatedAt(account.getUpdatedAt());
    response.setName(account.getName());

    return response;
  }

  public List<AccountResponse> getAccountsByUserId(@NonNull UUID userId) {
    var accounts = accountRepository.findByUserId(userId);

    return accounts.stream().map(this::toAccountResponse).toList();
  }

  public AccountResponse createAccount(
      @NonNull UUID userId, @NonNull AccountRequest accountRequest) {
    var user = userService.getUserReferenceById(userId);

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

    var response = toAccountResponse(account);

    log.info(
        "Created new account {}, {} of type {}",
        response.getAccountId(),
        response.getName(),
        response.getAccountType());

    return response;
  }

  public AccountResponse getAccount(@NonNull UUID userId, @NonNull UUID accountId) {
    var account =
        accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::forAccount);

    if (account.getUser().getId().equals(userId)) {
      log.info("Found account {} for user {}", accountId, userId);
      return toAccountResponse(account);
    }

    log.info("Account {} does not belong to user {}", accountId, userId);

    throw ResourceNotFoundException.forAccount();
  }

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
    account.setCurrency(accountRequest.getCurrency());
    account.setVirtual(accountRequest.isVirtual());
    account.setName(accountRequest.getName());

    accountRepository.save(account);

    log.info("Updated account {}", accountId);

    return toAccountResponse(account);
  }

  public void deleteAccount(@NonNull UUID userId, @NonNull UUID accountId) {
    var account =
        accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::forAccount);

    if (!account.getUser().getId().equals(userId)) {
      throw ResourceNotFoundException.forAccount();
    }

    accountRepository.delete(account);

    log.info("Account {} deleted for user {}", accountId, userId);
  }
}
