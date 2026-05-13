package cc.suvankar.moneytrail.contact

import cc.suvankar.moneytrail.user.User
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "contacts")
@EntityListeners(AuditingEntityListener::class)
class Contact(
  @Id @UuidGenerator @Column(name = "id", nullable = false, updatable = false) val id: UUID? = null,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  var user: User? = null,
  @Column(name = "name", length = 100, nullable = false) var name: String,
  @Column(name = "email", length = 255) var email: String? = null,
  @Column(name = "phone", length = 20) var phoneNo: String? = null,
  @Column(name = "notes") var notes: String? = null,
  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  var createdAt: OffsetDateTime? = null,
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  var updatedAt: OffsetDateTime? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (other !is Contact) return false
    return other.id == id
  }

  override fun hashCode(): Int = Objects.hash(id)
}
