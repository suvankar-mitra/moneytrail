package cc.suvankar.moneytrail.contact.dto

import jakarta.validation.constraints.*

data class ContactRequest(
  @field:NotBlank("Name is required.") val name: String,
  @field:Email(regexp = ".+@.+\\..+", message = "Must be a valid email address") val email: String?,
  @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
  val phoneNo: String?,
  val notes: String?,
)
