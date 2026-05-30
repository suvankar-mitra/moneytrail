package cc.suvankar.moneytrail.transaction;

import cc.suvankar.moneytrail.account.Account;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository
    extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
  boolean existsByFromAccountOrToAccount(Account fromAccount, Account toAccount);
}
