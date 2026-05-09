package cc.suvankar.moneytrail.contact

import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ContactRepository : JpaRepository<Contact, UUID> {
  fun findByUserId(userId: UUID): List<Contact>

  fun findByUserIdAndId(userId: UUID, id: UUID): Optional<Contact>
}
