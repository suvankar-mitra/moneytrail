package cc.suvankar.moneytrail.exchangerate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, ExchangeRateId> {}
