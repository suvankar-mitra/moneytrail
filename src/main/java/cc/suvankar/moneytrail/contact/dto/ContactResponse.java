package cc.suvankar.moneytrail.contact.dto;

import cc.suvankar.moneytrail.contact.Contact;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
  private UUID contactId;
  private String name;
  private String email;
  private String phoneNo;
  private String notes;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public static ContactResponse from(Contact contact) {
    return new ContactResponse(
        Objects.requireNonNull(contact.getId(), "Contact has no id"),
        contact.getName(),
        contact.getEmail(),
        contact.getPhoneNo(),
        contact.getNotes(),
        Objects.requireNonNull(contact.getCreatedAt(), "Contact has no createdAt"),
        Objects.requireNonNull(contact.getUpdatedAt(), "Contact has no updatedAt"));
  }
}
