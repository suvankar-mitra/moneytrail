package cc.suvankar.moneytrail.contact;

import static org.assertj.core.api.Assertions.assertThat;

import cc.suvankar.moneytrail.config.JpaConfig;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
public class ContactRepositoryTest {

  @Autowired private ContactRepository contactRepository;

  @Autowired private UserRepository userRepository;

  private User user;

  @BeforeEach
  void setup() {
    user = new User();
    user.setEmail("test@dummy.com");
    user.setName("John Doe");
    user.setPasswordHash("hashed_password");
    userRepository.save(user);
  }

  @Test
  public void findByUserId_shouldReturnContact_whenUserHasContact() {
    // Arrange
    Contact contact = new Contact();
    contact.setName("Contact 1");
    contact.setUser(user);
    contact.setEmail("contact1@example.com");
    contactRepository.save(contact);

    // Act
    List<Contact> result = contactRepository.findByUserId(user.getId());

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result.getFirst().getEmail()).isEqualTo("contact1@example.com");
  }

  @Test
  public void findByUserId_shouldReturnListOfContacts_whenUserHasMultipleContacts() {
    // Arrange
    Contact contact = new Contact();
    contact.setName("Contact 1");
    contact.setUser(user);
    contact.setEmail("contact1@example.com");
    contactRepository.save(contact);

    Contact contact2 = new Contact();
    contact2.setName("Contact 2");
    contact2.setUser(user);
    contact2.setEmail("contact2@example.com");
    contactRepository.save(contact2);

    // Act
    List<Contact> result = contactRepository.findByUserId(user.getId());

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  public void findByUserId_shouldBeEmpty_whenUserDoesNotHaveContact() {
    // Act
    List<Contact> result = contactRepository.findByUserId(user.getId());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void findByUserId_shouldReturnOnlyUser1Contact_whenMultipleUserHaveContacts() {
    // Arrange
    User user1 = new User();
    user1.setEmail("user1@dummy.com");
    user1.setName("User 1");
    user1.setPasswordHash("hashed_password");
    userRepository.save(user1);

    User user2 = new User();
    user2.setEmail("user2@dummy.com");
    user2.setName("User 2");
    user2.setPasswordHash("hashed_password");
    userRepository.save(user2);

    Contact contact = new Contact();
    contact.setName("Contact 1");
    contact.setUser(user1);
    contact.setEmail("contact1@example.com");
    contactRepository.save(contact);

    Contact contact2 = new Contact();
    contact2.setName("Contact 2");
    contact2.setUser(user2);
    contact2.setEmail("contact2@example.com");
    contactRepository.save(contact2);

    // Act
    List<Contact> result = contactRepository.findByUserId(user2.getId());

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUser()).isEqualTo(user2);
  }
}
