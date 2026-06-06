package cc.suvankar.moneytrail.transaction;

import cc.suvankar.moneytrail.account.Account;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
    extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
  boolean existsByFromAccountOrToAccount(Account fromAccount, Account toAccount);

  Optional<Transaction> findByIdAndFromAccount_UserIdAndToAccount_UserId(
      UUID id, UUID fromAccountUserId, UUID toAccountUserId);
}
