package cc.suvankar.moneytrail.tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserId(UUID userId);

    Optional<Tag> findByUserIdAndTagName(UUID userId, String tagName);
}
