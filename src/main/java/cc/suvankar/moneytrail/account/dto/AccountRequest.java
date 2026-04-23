package cc.suvankar.moneytrail.account.dto;

import cc.suvankar.moneytrail.account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountRequest {
  private UUID contactId;

  @NotBlank(message = "Account name is required.")
  private String name;

  @NotNull(message = "Account type is required.")
  private AccountType accountType;

  @NotBlank(message = "Currency is required.")
  @Size(min = 3, max = 3, message = "Currency should be 3 chars e.g. INR.")
  private String currency;

  private boolean virtual;
}
