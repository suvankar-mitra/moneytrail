package cc.suvankar.moneytrail.contact;

import cc.suvankar.moneytrail.contact.dto.ContactResponse;
import cc.suvankar.moneytrail.user.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "contacts")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Contact {
  @Id
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "name", length = 100, nullable = false)
  private String name;

  @Column(name = "email")
  private String email;

  @Column(name = "phone", length = 20)
  private String phoneNo;

  @Column(name = "notes")
  private String notes;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Override
  public boolean equals(Object other) {
    if (other instanceof Contact that) {
      return that.id.equals(this.id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static ContactResponse toResponse(Contact contact) {
    return ContactResponse.from(contact);
  }
}
