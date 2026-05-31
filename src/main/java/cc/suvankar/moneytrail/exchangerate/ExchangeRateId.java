package cc.suvankar.moneytrail.exchangerate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ExchangeRateId implements Serializable {
  @Column(name = "business_date", nullable = false)
  private LocalDate businessDate;

  @Column(name = "base_currency", length = 10, nullable = false)
  private String baseCurrency;

  @Column(name = "to_currency", length = 10, nullable = false)
  private String toCurrency;

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof ExchangeRateId other) {
      return other.businessDate.equals(this.businessDate)
          && other.baseCurrency.equals(this.baseCurrency)
          && other.toCurrency.equals(this.toCurrency);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessDate, baseCurrency, toCurrency);
  }
}
