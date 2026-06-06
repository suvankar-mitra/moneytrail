package cc.suvankar.moneytrail.tag;

import cc.suvankar.moneytrail.exception.InvalidCredentialsException;
import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
import cc.suvankar.moneytrail.tag.dto.TagRequest;
import cc.suvankar.moneytrail.tag.dto.TagResponse;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TagService {
  private final TagRepository repository;
  private final UserService userService;

  public TagService(TagRepository repository, UserService userService) {
    this.repository = repository;
    this.userService = userService;
  }

  @Transactional
  public TagResponse createTag(@NonNull UUID userId, @NonNull TagRequest request) {
    User user;

    try {
      user = userService.getUserById(userId);
    } catch (ResourceNotFoundException e) {
      throw new InvalidCredentialsException("Invalid credentials.");
    }

    Tag tag = new Tag();
    tag.setTagName(request.tagName());
    tag.setUser(user);
    repository.save(tag);

    log.info("Saved tag {} for userId {}", tag.getTagName(), userId);

    return TagResponse.from(tag);
  }

  @Transactional(readOnly = true)
  public TagResponse getTagResponse(@NonNull UUID userId, @NonNull String tagName) {
    Tag foundTag =
        repository
            .findByUserIdAndTagName(userId, tagName)
            .orElseThrow(ResourceNotFoundException::forTag);

    log.info("Found tag for user {} and tag name {}", userId, tagName);

    return TagResponse.from(foundTag);
  }

  public Tag getTag(@NonNull UUID userId, @NonNull Long tagId) {
    return repository
        .findByUserIdAndId(userId, tagId)
        .orElseThrow(ResourceNotFoundException::forTag);
  }

  @Transactional(readOnly = true)
  public List<TagResponse> getAllTagResponses(@NonNull UUID userId) {
    List<Tag> foundTags = repository.findByUserId(userId);

    log.info("Found all tags for user {}", userId);

    return foundTags.stream().map(TagResponse::from).toList();
  }

  @Transactional
  public void deleteTag(@NonNull UUID userId, @NonNull String tagName) {
    Tag foundTag =
        repository
            .findByUserIdAndTagName(userId, tagName)
            .orElseThrow(ResourceNotFoundException::forTag);

    log.info("Deleting tag for user {} and tag name {}", userId, tagName);

    repository.delete(foundTag);
  }
}
