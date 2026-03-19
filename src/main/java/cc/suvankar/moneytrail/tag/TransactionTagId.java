package cc.suvankar.moneytrail.tag;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class TransactionTagId implements Serializable {
    private UUID transactionId;
    private Long tagId;

    @Override
    public boolean equals(Object obj) {
        if (transactionId == null || tagId == null) {
            return false;
        }

        if (obj instanceof TransactionTagId other) {
            return (other.transactionId.equals(transactionId) && other.tagId.equals(tagId));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, tagId);
    }

}
