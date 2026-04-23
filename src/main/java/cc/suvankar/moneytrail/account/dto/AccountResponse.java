package cc.suvankar.moneytrail.account.dto;

import cc.suvankar.moneytrail.account.AccountType;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountResponse {
  private UUID accountId;
  private UUID contactId;
  private String name;
  private AccountType accountType;
  private String currency;
  private boolean virtual;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
