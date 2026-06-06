package cc.suvankar.moneytrail.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import cc.suvankar.moneytrail.account.Account;
import cc.suvankar.moneytrail.account.AccountRepository;
import cc.suvankar.moneytrail.account.AccountType;
import cc.suvankar.moneytrail.account.CurrencyCode;
import cc.suvankar.moneytrail.config.JpaConfig;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Import(JpaConfig.class)
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class TransactionRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

  private User user;
  private Account fromAccount;
  private Account toAccount;
  private Transaction transaction;

  @Autowired private AccountRepository accountRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private TransactionRepository transactionRepository;

  private Account buildAccount(User user, String name, AccountType type) {

    Account account = new Account();
    account.setUser(user);
    account.setName(name);
    account.setType(type);
    account.setCurrency(CurrencyCode.valueOf("INR"));
    account.setVirtual(false);
    accountRepository.save(account);

    return account;
  }

  @BeforeEach
  void setup() {
    user = new User();
    user.setEmail("test@dummy.com");
    user.setName("John Doe");
    user.setPasswordHash("hashed_password");
    userRepository.save(user);

    fromAccount = buildAccount(user, "Bank A account", AccountType.ASSET);
    toAccount = buildAccount(user, "Bank B account", AccountType.LIABILITY);

    transaction = new Transaction();
    transaction.setFromAccount(fromAccount);
    transaction.setToAccount(toAccount);
    transaction.setTranDate(LocalDate.now());
    transaction.setTransactionAmount(new BigDecimal("122.92"));
    transactionRepository.save(transaction);
  }

  @Test
  void findById_shouldReturnTransaction_withCorrectAccounts_whenTransactionExists() {
    // Act
    Optional<Transaction> result = transactionRepository.findById(transaction.getId());

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result.get().getFromAccount()).isEqualTo(fromAccount);
    assertThat(result.get().getToAccount()).isEqualTo(toAccount);
  }

  @Test
  void findById_shouldReturnTransaction_withCorrectAmount_whenTransactionExists() {
    // Act
    Optional<Transaction> result = transactionRepository.findById(transaction.getId());

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result.get().getTransactionAmount()).isEqualTo(new BigDecimal("122.92"));
  }
}
