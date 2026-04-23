package cc.suvankar.moneytrail.tag;

import cc.suvankar.moneytrail.transaction.Transaction;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction_tags")
public class TransactionTag {

  @EmbeddedId private TransactionTagId transactionTagId;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("transactionId")
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("tagId")
  @JoinColumn(name = "tag_id", nullable = false)
  private Tag tag;

  @Override
  public boolean equals(Object obj) {
    if (transactionTagId == null) {
      return false;
    }
    if (obj instanceof TransactionTag other) {
      return other.transactionTagId.equals(transactionTagId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return transactionTagId.hashCode();
  }
}
