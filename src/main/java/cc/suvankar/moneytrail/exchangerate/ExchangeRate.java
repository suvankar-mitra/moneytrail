package cc.suvankar.moneytrail.exchangerate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchange_rates")
@EntityListeners(AuditingEntityListener.class)
public class ExchangeRate {

  @EmbeddedId private ExchangeRateId exchangeRateId;

  @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
  private BigDecimal exchangeRate = BigDecimal.valueOf(1.0);

  @CreatedDate
  @Column(name = "fetched_at", nullable = false)
  private OffsetDateTime fetchedAt;

  @Column(name = "source", nullable = false)
  private String source;

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof ExchangeRate other) {
      return other.exchangeRateId.equals(this.exchangeRateId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.exchangeRateId.hashCode();
  }
}
