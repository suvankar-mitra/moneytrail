package cc.suvankar.moneytrail.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Set;

public record TransactionUpdateRequest(
    @NotNull(message = "transactionAmount is required.")
        @Positive(message = "transactionAmount must be Positive.")
        BigDecimal transactionAmount,
    @NotNull(message = "tagIdSet cannot be null.") Set<Long> tagIdSet,
    String note) {}
