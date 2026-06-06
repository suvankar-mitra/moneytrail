package cc.suvankar.moneytrail.user;

import static org.assertj.core.api.Assertions.assertThat;

import cc.suvankar.moneytrail.config.JpaConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.*;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.junit.jupiter.Container;

@DataJpaTest
@Import(JpaConfig.class)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

  @Autowired private UserRepository userRepository;

  @Test
  void findByEmail_shouldReturnUser_whenEmailExists() {
    // Arrange
    User user = new User();
    user.setEmail("test@dummy.com");
    user.setName("John Doe");
    user.setPasswordHash("hashed_password");
    userRepository.save(user);

    // Act
    Optional<User> result = userRepository.findByEmail("test@dummy.com");

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("test@dummy.com");
  }

  @Test
  void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
    // Arrange..not required

    // Act
    Optional<User> result = userRepository.findByEmail("test@example.com");

    // Assert
    assertThat(result).isEmpty();
  }
}
