package cc.suvankar.moneytrail.transaction.dto;

import cc.suvankar.moneytrail.tag.*;
import cc.suvankar.moneytrail.transaction.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record TransactionResponse(
    UUID transactionId,
    UUID fromAccount,
    UUID toAccount,
    BigDecimal transactionAmount,
    BigDecimal effectiveAmount,
    BigDecimal exchangeRate,
    Set<Long> tagIdSet,
    LocalDate tranDate,
    String note) {
  public static TransactionResponse from(Transaction tran) {
    return new TransactionResponse(
        tran.getId(),
        tran.getFromAccount().getId(),
        tran.getToAccount().getId(),
        tran.getTransactionAmount(),
        tran.getTransactionAmount().multiply(tran.getExchangeRate()),
        tran.getExchangeRate(),
        tran.getTags().stream().map(Tag::getId).collect(Collectors.toSet()),
        tran.getTranDate(),
        tran.getNote());
  }
}
