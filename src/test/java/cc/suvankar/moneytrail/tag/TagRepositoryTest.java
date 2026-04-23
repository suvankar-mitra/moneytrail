package cc.suvankar.moneytrail.tag;

import static org.assertj.core.api.Assertions.assertThat;

import cc.suvankar.moneytrail.config.JpaConfig;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
public class TagRepositoryTest {

  private User user;
  private Tag tag;

  @Autowired private UserRepository userRepository;

  @Autowired private TagRepository tagRepository;

  @BeforeEach
  void setup() {
    user = new User();
    user.setEmail("test@dummy.com");
    user.setName("John Doe");
    user.setPasswordHash("hashed_password");
    userRepository.save(user);

    tag = new Tag();
    tag.setUser(user);
    tag.setTagName("food");
    tagRepository.save(tag);
  }

  @Test
  void findByUserId_shouldReturnTag_whenUserHasTag() {
    // Act
    List<Tag> result = tagRepository.findByUserId(user.getId());

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUser()).isEqualTo(user);
    assertThat(result.getFirst().getTagName()).isEqualTo(tag.getTagName());
  }

  @Test
  void findByUserIdAndTagName_shouldReturnTag_whenUserHasTagName() {
    // Act
    Optional<Tag> result = tagRepository.findByUserIdAndTagName(user.getId(), tag.getTagName());

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result.get()).isEqualTo(tag);
  }

  @Test
  void findByUserId_shouldReturnEmpty_whenUserDoesNotHaveAnyTag() {
    // Act
    List<Tag> result = tagRepository.findByUserId(UUID.randomUUID());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByUserIdAndTagName_shouldReturnEmpty_whenUserDoesNotHaveThisTag() {
    // Act
    Optional<Tag> result = tagRepository.findByUserIdAndTagName(user.getId(), "grocery");

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByUserId_shouldReturnUser1Tag_whenMultipleUsersHaveTags() {
    // Arrange
    User user2 = new User();
    user2.setEmail("test2@dummy.com");
    user2.setName("John Doe");
    user2.setPasswordHash("hashed_password");
    userRepository.save(user2);

    Tag tag2 = new Tag();
    tag2.setUser(user2);
    tag2.setTagName("grocery");
    tagRepository.save(tag2);

    // Act
    List<Tag> result = tagRepository.findByUserId(user2.getId());

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result.getFirst().getTagName()).isEqualTo("grocery");
  }
}
