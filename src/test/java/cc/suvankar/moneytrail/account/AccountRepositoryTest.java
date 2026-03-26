package cc.suvankar.moneytrail.account;

import cc.suvankar.moneytrail.config.JpaConfig;
import cc.suvankar.moneytrail.contact.Contact;
import cc.suvankar.moneytrail.contact.ContactRepository;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class AccountRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User user;
    private Contact contact;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setEmail("test@dummy.com");
        user.setName("John Doe");
        user.setPasswordHash("hashed_password");
        userRepository.save(user);

        contact = new Contact();
        contact.setName("Contact 1");
        contact.setUser(user);
        contact.setEmail("contact1@example.com");
        contactRepository.save(contact);
    }

    private Account buildAccount(User user, Contact contact) {
        Account account = new Account();
        account.setUser(user);
        account.setName("Test Account");
        account.setType(AccountType.ASSET);
        account.setCurrency("INR");
        account.setVirtual(false);
        account.setContact(contact);
        return account;
    }

    @Test
    public void findByUserId_shouldReturnAccount_whenUserHasAccount() {
        // Arrange
        Account account = buildAccount(user, null);
        accountRepository.save(account);

        // Act
        List<Account> result = accountRepository.findByUserId(user.getId());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser()).isEqualTo(user);

    }

    @Test
    public void findByUserId_shouldBeEmpty_whenUserDoesNotHaveAccount() {
        // Act
        List<Account> result = accountRepository.findByUserId(user.getId());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void findByUserId_shouldHaveContact_whenAccountLinkedToContact() {
        // Arrange
        Account account = buildAccount(user, contact);
        accountRepository.save(account);

        // Act
        List<Account> result = accountRepository.findByUserId(user.getId());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getContact()).isEqualTo(contact);
    }

    @Test
    public void findByUserId_shouldReturnOnlyUser1Account_whenMultipleUserHaveAccounts() {
        // Arrange
        User user1 = new User();
        user1.setEmail("test1@dummy.com");
        user1.setName("John Doe");
        user1.setPasswordHash("hashed_password");
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("test2@dummy.com");
        user2.setName("John Doe");
        user2.setPasswordHash("hashed_password");
        userRepository.save(user2);

        Account account1 = buildAccount(user1, null);
        accountRepository.save(account1);

        Account account2 = buildAccount(user2, null);
        accountRepository.save(account2);

        // Act
        List<Account> result = accountRepository.findByUserId(user1.getId());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser()).isEqualTo(user1);
    }

}
