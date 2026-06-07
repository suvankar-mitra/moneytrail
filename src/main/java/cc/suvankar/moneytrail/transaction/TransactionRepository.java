package cc.suvankar.moneytrail.transaction;

import cc.suvankar.moneytrail.account.Account;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.lang.*;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
    extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
  boolean existsByFromAccountOrToAccount(Account fromAccount, Account toAccount);

  Optional<Transaction> findByIdAndFromAccount_UserIdAndToAccount_UserId(
      UUID id, UUID fromAccountUserId, UUID toAccountUserId);

  @NonNull
  @EntityGraph(attributePaths = {"fromAccount", "toAccount"})
  @Override
  Page<Transaction> findAll(Specification<Transaction> spec, @NonNull Pageable pageable);
}
