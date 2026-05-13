package cc.suvankar.moneytrail.user

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
  @Id @UuidGenerator @Column("id", nullable = false, updatable = false) var id: UUID? = null,
  @Column(name = "name", length = 100, nullable = false) var name: String,
  @Column(name = "email", nullable = false) var email: String,
  @Column(name = "password_hash", nullable = false) var passwordHash: String,
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  var createdAt: OffsetDateTime? = null,
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  var updatedAt: OffsetDateTime? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (other !is User) return false
    return other.id == id
  }

  override fun hashCode(): Int {
    return Objects.hash(id)
  }
}
