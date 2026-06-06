package cc.suvankar.moneytrail.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record TransactionRequest(
    @NotNull(message = "fromAccountId is required.") UUID fromAccountId,
    @NotNull(message = "toAccountId is required.") UUID toAccountId,
    @NotNull(message = "transactionAmount is required.")
        @Positive(message = "transactionAmount must be Positive.")
        BigDecimal transactionAmount,
    @NotNull(message = "tagIdSet cannot be null.") Set<Long> tagIdSet,
    @NotNull(message = "tranDate is required.") LocalDate tranDate,
    String note) {}
