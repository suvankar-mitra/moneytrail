package cc.suvankar.moneytrail.contact;

import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ContactService {
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Contact getContactReferenceById(UUID id) {
        return contactRepository.getReferenceById(id);
    }

    public Contact getContact(UUID userId, UUID contactId) {
        return contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(ResourceNotFoundException::forContact);
    }
}
