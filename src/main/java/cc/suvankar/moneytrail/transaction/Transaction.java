package cc.suvankar.moneytrail.transaction;

import cc.suvankar.moneytrail.account.Account;
import cc.suvankar.moneytrail.tag.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
  @Id
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_account_id", nullable = false)
  private Account fromAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_account_id", nullable = false)
  private Account toAccount;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "transaction_tags",
      joinColumns = @JoinColumn(name = "transaction_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @BatchSize(size = 25)
  private Set<Tag> tags = new HashSet<>();

  @Column(name = "transaction_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal transactionAmount;

  @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
  private BigDecimal exchangeRate = BigDecimal.valueOf(1.0);

  @Column(name = "tran_date", nullable = false)
  private LocalDate tranDate;

  @Column(name = "note")
  private String note;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Override
  public boolean equals(Object obj) {
    if (id == null) {
      return false;
    }
    if (obj instanceof Transaction other) {
      return other.id.equals(id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
