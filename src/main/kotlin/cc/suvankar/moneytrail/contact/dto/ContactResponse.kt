package cc.suvankar.moneytrail.contact.dto

import cc.suvankar.moneytrail.contact.Contact
import java.time.OffsetDateTime
import java.util.UUID

data class ContactResponse(
  val id: UUID,
  val name: String,
  val email: String?,
  val phoneNo: String?,
  val notes: String?,
  val createdAt: OffsetDateTime,
  val updatedAt: OffsetDateTime,
) {
  companion object {
    @JvmStatic
    fun from(contact: Contact) =
      ContactResponse(
        id = contact.id ?: error("Contact has no id"),
        name = contact.name,
        email = contact.email,
        phoneNo = contact.phoneNo,
        notes = contact.notes,
        createdAt = contact.createdAt ?: error("Contact has no createdAt"),
        updatedAt = contact.updatedAt ?: error("Contact has no updatedAt"),
      )
  }
}
