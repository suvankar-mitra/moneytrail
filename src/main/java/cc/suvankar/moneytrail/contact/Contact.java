package cc.suvankar.moneytrail.contact;

import cc.suvankar.moneytrail.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contacts")
@EntityListeners(AuditingEntityListener.class)
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

  @Column(name = "email", length = 255)
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
  public boolean equals(Object obj) {
    if (id == null) {
      return false;
    }
    if (obj instanceof Contact other) {
      return other.id.equals(id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
