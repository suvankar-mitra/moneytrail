package cc.suvankar.moneytrail.contact;

import cc.suvankar.moneytrail.contact.dto.ContactRequest;
import cc.suvankar.moneytrail.contact.dto.ContactResponse;
import cc.suvankar.moneytrail.exception.InvalidCredentialsException;
import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ContactService {

  private final ContactRepository contactRepository;
  private final UserService userService;

  public ContactService(ContactRepository contactRepository, UserService userService) {
    this.contactRepository = contactRepository;
    this.userService = userService;
  }

  @Transactional(readOnly = true)
  public Contact getContact(@NonNull UUID userId, @NonNull UUID contactId) {
    return contactRepository
        .findByUserIdAndId(userId, contactId)
        .orElseThrow(ResourceNotFoundException::forContact);
  }

  @Transactional(readOnly = true)
  public ContactResponse getContactResponse(@NonNull UUID userId, @NonNull UUID contactId) {
    return ContactResponse.from(getContact(userId, contactId));
  }

  @Transactional(readOnly = true)
  public List<ContactResponse> getContactsByUserId(@NonNull UUID userId) {
    return contactRepository.findByUserId(userId).stream().map(Contact::toResponse).toList();
  }

  @Transactional
  public ContactResponse createContact(@NonNull UUID userId, @NonNull ContactRequest request) {
    User user;

    try {
      user = userService.getUserById(userId);
    } catch (ResourceNotFoundException e) {
      throw new InvalidCredentialsException("Invalid credentials.");
    }

    var contact = new Contact();
    contact.setUser(user);
    contact.setName(request.getName());
    contact.setEmail(request.getEmail());
    contact.setPhoneNo(request.getPhoneNo());
    contact.setNotes(request.getNotes());

    log.info("Saving contact for user {}", userId);
    contact = contactRepository.save(contact);
    log.info("Saved contact {} for user {}", contact.getId(), userId);

    return ContactResponse.from(contact);
  }

  @Transactional
  public ContactResponse updateContact(
      @NonNull UUID userId, @NonNull UUID contactId, @NonNull ContactRequest request) {
    var contact = getContact(userId, contactId);

    contact.setName(request.getName());
    contact.setEmail(request.getEmail());
    contact.setPhoneNo(request.getPhoneNo());
    contact.setNotes(request.getNotes());

    log.info("Updating contact {} for user {}", contactId, userId);
    contact = contactRepository.save(contact);
    log.info("Updated contact {} for user {}", contactId, userId);

    return ContactResponse.from(contact);
  }

  @Transactional
  public void deleteContact(@NonNull UUID userId, @NonNull UUID contactId) {
    var contact = getContact(userId, contactId);
    log.info("Deleting contact {} for user {}", contactId, userId);
    contactRepository.delete(contact);
    log.info("Deleted contact {} for user {}", contactId, userId);
  }
}
