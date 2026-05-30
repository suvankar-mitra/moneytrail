package cc.suvankar.moneytrail.account.dto;

import cc.suvankar.moneytrail.account.Account;
import cc.suvankar.moneytrail.account.AccountStatus;
import cc.suvankar.moneytrail.account.AccountType;
import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
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
  private AccountStatus accountStatus;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public static AccountResponse from(final Account account) {
    AccountResponse response = new AccountResponse();

    if (account.getType() == AccountType.RECEIVABLE || account.getType() == AccountType.PAYABLE) {
      if (account.getContact() == null) {
        throw ResourceNotFoundException.forContact();
      }
      response.setContactId(account.getContact().getId());
    }
    response.setAccountId(account.getId());
    response.setName(account.getName());
    response.setAccountType(account.getType());
    response.setCurrency(account.getCurrency());
    response.setVirtual(account.isVirtual());
    response.setAccountStatus(account.getAccountStatus());
    response.setCreatedAt(account.getCreatedAt());
    response.setUpdatedAt(account.getUpdatedAt());
    return response;
  }
}
