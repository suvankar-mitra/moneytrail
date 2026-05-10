package cc.suvankar.moneytrail.contact

import cc.suvankar.moneytrail.contact.dto.ContactRequest
import cc.suvankar.moneytrail.contact.dto.ContactResponse
import cc.suvankar.moneytrail.user.UserPrincipal
import jakarta.validation.Valid
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ContactController(val contactService: ContactService) {
  private val log = LoggerFactory.getLogger(ContactController::class.java)

  @GetMapping("/contacts")
  fun getContacts(
    @AuthenticationPrincipal userPrincipal: UserPrincipal
  ): ResponseEntity<List<ContactResponse>> {
    val userId = userPrincipal.userId
    log.info("Getting all contacts for user {}", userId)

    return ResponseEntity.ok(contactService.getContactsByUserId(userId = userId))
  }

  @PostMapping("/contacts")
  fun createContact(
    @AuthenticationPrincipal userPrincipal: UserPrincipal,
    @RequestBody @Valid request: ContactRequest,
  ): ResponseEntity<ContactResponse> {
    val userId = userPrincipal.userId
    log.info("Creating new contact for user {}", userId)

    val response = contactService.createContact(userId = userId, request = request)

    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @GetMapping("/contacts/{id}")
  fun getContact(
    @AuthenticationPrincipal userPrincipal: UserPrincipal,
    @PathVariable id: UUID,
  ): ResponseEntity<ContactResponse> {
    val userId = userPrincipal.userId
    log.info("Getting contacts fo user {} and id {}", userId, id)

    val response = contactService.getContactResponse(userId, contactId = id)
    return ResponseEntity.ok(response)
  }

  @PutMapping("/contacts/{id}")
  fun updateContact(
    @AuthenticationPrincipal userPrincipal: UserPrincipal,
    @PathVariable id: UUID,
    @RequestBody request: ContactRequest,
  ): ResponseEntity<ContactResponse> {
    val userId = userPrincipal.userId
    log.info("Updating contact for user {} and id {}", userId, id)

    val response = contactService.updateContact(userId = userId, id, request = request)
    return ResponseEntity.ok(response)
  }

  @DeleteMapping("/contacts/{id}")
  fun deleteContact(@AuthenticationPrincipal userPrincipal: UserPrincipal, @PathVariable id: UUID) {
    val userId = userPrincipal.userId
    log.info("Deleting contact for user {} and id {}", userId, id)

    contactService.deleteContact(userId = userId, contactId = id)
  }
}
