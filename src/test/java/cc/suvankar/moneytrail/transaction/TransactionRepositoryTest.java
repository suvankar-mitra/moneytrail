package cc.suvankar.moneytrail.transaction;

import cc.suvankar.moneytrail.account.Account;
import cc.suvankar.moneytrail.account.AccountRepository;
import cc.suvankar.moneytrail.account.AccountType;
import cc.suvankar.moneytrail.config.JpaConfig;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class TransactionRepositoryTest {

    private User user;
    private Account fromAccount;
    private Account toAccount;
    private Transaction transaction;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account buildAccount(User user, String name, AccountType type) {

        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        account.setType(type);
        account.setCurrency("INR");
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
        transaction.setTranDate(OffsetDateTime.now());
        transaction.setAmount(new BigDecimal("122.92"));
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
        assertThat(result.get().getAmount()).isEqualTo(new BigDecimal("122.92"));
    }

}
