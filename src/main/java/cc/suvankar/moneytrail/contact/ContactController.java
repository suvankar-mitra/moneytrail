package cc.suvankar.moneytrail.contact;

import cc.suvankar.moneytrail.contact.dto.ContactRequest;
import cc.suvankar.moneytrail.contact.dto.ContactResponse;
import cc.suvankar.moneytrail.user.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ContactController {
  private final ContactService contactService;

  public ContactController(ContactService contactService) {
    this.contactService = contactService;
  }

  @GetMapping("/contacts")
  public ResponseEntity<List<ContactResponse>> getContacts(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal) {
    var userId = userPrincipal.userId();
    log.info("Getting all contacts for user {}", userId);
    return ResponseEntity.ok(contactService.getContactsByUserId(userId));
  }

  @PostMapping("/contacts")
  public ResponseEntity<ContactResponse> createContact(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @Valid @RequestBody ContactRequest request) {
    var userId = userPrincipal.userId();
    log.info("Creating new contact for user {}", userId);
    var response = contactService.createContact(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/contacts/{id}")
  public ResponseEntity<ContactResponse> getContact(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable UUID id) {
    var userId = userPrincipal.userId();
    log.info("Getting contact fo user {} and id {}", userId, id);
    var response = contactService.getContactResponse(userId, id);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/contacts/{id}")
  public ResponseEntity<ContactResponse> updateContact(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable UUID id,
      @NonNull @Valid @RequestBody ContactRequest request) {
    var userId = userPrincipal.userId();
    log.info("Updating contact {} for user {}", id, userId);
    var response = contactService.updateContact(userId, id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/contacts/{id}")
  public ResponseEntity<Void> deleteContact(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable UUID id) {
    var userId = userPrincipal.userId();
    log.info("Deleting contact {} for user {}", id, userId);
    contactService.deleteContact(userId, id);
    return ResponseEntity.noContent().build();
  }
}
