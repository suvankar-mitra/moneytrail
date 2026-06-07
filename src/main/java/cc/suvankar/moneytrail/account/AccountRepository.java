package cc.suvankar.moneytrail.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

  List<Account> findByUserId(UUID userId);

  Optional<Account> findByUserIdAndId(UUID userId, UUID id);

  Optional<Account> findByUserIdAndTypeAndCurrency(
      UUID userId, AccountType accountType, CurrencyCode currency);

  List<Account> findByUserIdAndTypeNot(UUID userId, AccountType type);

  List<Account> findByUserIdAndVirtualFalse(UUID userId);

  List<Account> findByUserIdAndCurrencyAndVirtual(
      UUID userId, CurrencyCode currency, boolean virtual);
}
