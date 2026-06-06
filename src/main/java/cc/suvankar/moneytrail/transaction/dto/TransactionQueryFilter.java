package cc.suvankar.moneytrail.transaction.dto;

import java.time.LocalDate;

public record TransactionQueryFilter(LocalDate startDate, LocalDate endDate) {}
