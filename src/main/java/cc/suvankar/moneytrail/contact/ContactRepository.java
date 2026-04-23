package cc.suvankar.moneytrail.contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

  List<Contact> findByUserId(UUID userId);

  Optional<Contact> findByUserIdAndId(UUID userId, UUID id);
}
