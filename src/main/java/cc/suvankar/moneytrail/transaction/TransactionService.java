package cc.suvankar.moneytrail.transaction;

import cc.suvankar.moneytrail.account.Account;
import cc.suvankar.moneytrail.account.AccountService;
import cc.suvankar.moneytrail.account.AccountStatus;
import cc.suvankar.moneytrail.exception.AccountNotActiveException;
import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
import cc.suvankar.moneytrail.exception.TransactionFilterException;
import cc.suvankar.moneytrail.exchangerate.ExchangeRateService;
import cc.suvankar.moneytrail.tag.Tag;
import cc.suvankar.moneytrail.tag.TagService;
import cc.suvankar.moneytrail.transaction.dto.TransactionQueryFilter;
import cc.suvankar.moneytrail.transaction.dto.TransactionRequest;
import cc.suvankar.moneytrail.transaction.dto.TransactionResponse;
import cc.suvankar.moneytrail.transaction.dto.TransactionUpdateRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TransactionService {
  private final TransactionRepository repository;
  private final AccountService accountService;
  private final TagService tagService;
  private final ExchangeRateService exchangeRateService;

  public TransactionService(
      TransactionRepository repository,
      AccountService accountService,
      TagService tagService,
      ExchangeRateService exchangeRateService) {
    this.repository = repository;
    this.accountService = accountService;
    this.tagService = tagService;
    this.exchangeRateService = exchangeRateService;
  }

  @Transactional
  public TransactionResponse createTransaction(
      @NonNull UUID userId, @NonNull TransactionRequest request) {
    // Get from-account
    var fromAccount =
        accountService.findAccountByUserIdAndAccountId(userId, request.fromAccountId());
    if (isAccountInactive(fromAccount)) {
      throw new AccountNotActiveException(fromAccount.getId().toString());
    }

    // Get to-account
    var toAccount = accountService.findAccountByUserIdAndAccountId(userId, request.toAccountId());
    if (isAccountInactive(toAccount)) {
      throw new AccountNotActiveException(toAccount.getId().toString());
    }

    // Get exchange rate for transaction date
    var exchangeRate = getExchangeRate(request.tranDate(), fromAccount, toAccount);

    // Get Tags
    Set<Tag> tags = getTagSet(userId, request.tagIdSet());

    // Create Transaction and persist
    Transaction transaction = new Transaction();
    transaction.setFromAccount(fromAccount);
    transaction.setToAccount(toAccount);
    transaction.setExchangeRate(exchangeRate);
    transaction.setTags(tags);
    transaction.setNote(request.note());
    transaction.setTranDate(request.tranDate());
    transaction.setTransactionAmount(request.transactionAmount());

    repository.save(transaction);

    return TransactionResponse.from(transaction);
  }

  @Transactional
  public TransactionResponse updateTransaction(
      @NonNull UUID userId,
      @NonNull UUID transactionId,
      @NonNull TransactionUpdateRequest request) {
    var foundTransaction =
        repository
            .findByIdAndFromAccount_UserIdAndToAccount_UserId(transactionId, userId, userId)
            .orElseThrow(ResourceNotFoundException::forTransaction);

    Set<Tag> tags = getTagSet(userId, request.tagIdSet());

    foundTransaction.setNote(request.note());
    foundTransaction.setTags(tags);
    foundTransaction.setTransactionAmount(request.transactionAmount());

    return TransactionResponse.from(foundTransaction);
  }

  @Transactional
  public void deleteTransaction(@NonNull UUID userId, @NonNull UUID transactionId) {
    var foundTransaction =
        repository
            .findByIdAndFromAccount_UserIdAndToAccount_UserId(transactionId, userId, userId)
            .orElseThrow(ResourceNotFoundException::forTransaction);

    repository.delete(foundTransaction);
  }

  @Transactional(readOnly = true)
  public TransactionResponse getTransaction(@NonNull UUID userId, @NonNull UUID tranId) {
    Transaction foundTran =
        repository
            .findByIdAndFromAccount_UserIdAndToAccount_UserId(tranId, userId, userId)
            .orElseThrow(ResourceNotFoundException::forTransaction);

    return TransactionResponse.from(foundTran);
  }

  @Transactional(readOnly = true)
  public Page<TransactionResponse> getTransactions(
      @NonNull UUID userId, @NonNull TransactionQueryFilter filter, Pageable pageable) {
    Specification<Transaction> spec = byFromAccountUserId(userId).and(byToAccountUserId(userId));

    // neither dates are null
    if (filter.startDate() != null && filter.endDate() != null) {
      spec = spec.and(byDateRange(filter.startDate(), filter.endDate()));
    }
    // one of the dates is null
    else if (filter.startDate() != null || filter.endDate() != null) {
      throw new TransactionFilterException(
          "Both start date and end date are required if either one is provided.");
    } else {
      // both dates are null
      pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
    }

    return repository.findAll(spec, pageable).map(TransactionResponse::from);
  }

  private Specification<Transaction> byFromAccountUserId(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("fromAccount").get("user").get("id"), userId);
  }

  private Specification<Transaction> byToAccountUserId(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("toAccount").get("user").get("id"), userId);
  }

  private Specification<Transaction> byDateRange(LocalDate startDate, LocalDate endDate) {
    return (root, query, cb) -> cb.between(root.get("tranDate"), startDate, endDate);
  }

  private Set<Tag> getTagSet(UUID userId, Set<Long> tagIdSet) {
    return tagIdSet.stream()
        .filter(Objects::nonNull)
        .map(tagId -> tagService.getTag(userId, tagId))
        .collect(Collectors.toSet());
  }

  private boolean isAccountInactive(Account account) {
    return account.getAccountStatus() != AccountStatus.ACTIVE;
  }

  private BigDecimal getExchangeRate(LocalDate date, Account fromAccount, Account toAccount) {
    // If same currency, exchange rate is 1.0
    if (fromAccount.getCurrency().equals(toAccount.getCurrency())) {
      return BigDecimal.ONE;
    }

    // Fetch and return exchange rate
    return exchangeRateService
        .getExchangeRate(
            date,
            fromAccount.getCurrency().toString().toLowerCase(),
            toAccount.getCurrency().toString().toLowerCase())
        .getExchangeRate();
  }
}
