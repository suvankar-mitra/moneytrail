package cc.suvankar.moneytrail.transaction.dto;

import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.tag.dto.TagResponse;
import cc.suvankar.moneytrail.transaction.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record TransactionResponse(
    UUID transactionId,
    AccountResponse fromAccount,
    AccountResponse toAccount,
    BigDecimal transactionAmount,
    BigDecimal effectiveAmount,
    BigDecimal exchangeRate,
    Set<TagResponse> tags,
    LocalDate tranDate,
    String note) {
  public static TransactionResponse from(Transaction tran) {
    return new TransactionResponse(
        tran.getId(),
        AccountResponse.from(tran.getFromAccount()),
        AccountResponse.from(tran.getToAccount()),
        tran.getTransactionAmount(),
        tran.getTransactionAmount().multiply(tran.getExchangeRate()),
        tran.getExchangeRate(),
        tran.getTags().stream().map(TagResponse::from).collect(Collectors.toSet()),
        tran.getTranDate(),
        tran.getNote());
  }
}
