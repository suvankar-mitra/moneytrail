package cc.suvankar.moneytrail.contact

import cc.suvankar.moneytrail.contact.dto.ContactRequest
import cc.suvankar.moneytrail.contact.dto.ContactResponse
import cc.suvankar.moneytrail.exception.ResourceNotFoundException
import cc.suvankar.moneytrail.user.UserService
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val userService: UserService,
) {
  private val log = LoggerFactory.getLogger(ContactService::class.java)

  fun getContact(userId: UUID, contactId: UUID): Contact {
    return contactRepository
      .findByUserIdAndId(userId = userId, id = contactId)
      .orElseThrow(ResourceNotFoundException::forContact)
  }

  fun getContactsByUserId(userId: UUID): List<ContactResponse> {
    return contactRepository.findByUserId(userId = userId).map(ContactResponse::from)
  }

  fun createContact(userId: UUID, request: ContactRequest): ContactResponse {
    val contact =
      Contact(
        user = userService.getUserReferenceById(userId),
        name = request.name,
        email = request.email,
        phoneNo = request.phoneNo,
        notes = request.notes,
      )
    contactRepository.save(contact)

    log.info("Saving contact for user {}", userId)

    return ContactResponse.from(contact)
  }

  fun updateContact(userId: UUID, contactId: UUID, request: ContactRequest): ContactResponse {
    val contact =
      contactRepository
        .findByUserIdAndId(userId = userId, id = contactId)
        .orElseThrow(ResourceNotFoundException::forContact)

    contact.name = request.name
    contact.email = request.email
    contact.phoneNo = request.phoneNo
    contact.notes = request.notes

    log.info("Updating contact {} for user {}", contactId, userId)

    return ContactResponse.from(contact)
  }

  fun deleteContact(userId: UUID, contactId: UUID) {
    val contact =
      contactRepository
        .findByUserIdAndId(userId = userId, id = contactId)
        .orElseThrow(ResourceNotFoundException::forContact)

    log.info("Deleting contact {} for user {}", contactId, userId)

    contactRepository.delete(contact)
  }
}
