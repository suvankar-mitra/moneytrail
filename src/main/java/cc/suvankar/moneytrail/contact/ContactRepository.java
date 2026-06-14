package cc.suvankar.moneytrail.contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ContactRepository extends JpaRepository<Contact, UUID> {
  List<Contact> findByUserId(@NonNull UUID userId);

  Optional<Contact> findByUserIdAndId(@NonNull UUID userId, @NonNull UUID id);
}
