package cc.suvankar.moneytrail.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {
  @NotBlank(message = "Name is required.")
  private String name;

  @Email(regexp = ".+@.+\\..+", message = "Must be a valid email address")
  private String email;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
  private String phoneNo;

  private String notes;
}
