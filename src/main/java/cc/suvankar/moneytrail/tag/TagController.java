package cc.suvankar.moneytrail.tag;

import cc.suvankar.moneytrail.tag.dto.TagRequest;
import cc.suvankar.moneytrail.tag.dto.TagResponse;
import cc.suvankar.moneytrail.user.UserPrincipal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class TagController {
  private final TagService service;

  public TagController(TagService service) {
    this.service = service;
  }

  @GetMapping("/tags")
  public ResponseEntity<List<TagResponse>> getTags(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal) {
    var userId = userPrincipal.userId();
    log.info("Getting all Tags for user {}", userId);
    var tags = service.getAllTagResponses(userId);

    return ResponseEntity.ok(tags);
  }

  @GetMapping("/tags/{name}")
  public ResponseEntity<TagResponse> getTag(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable String name) {
    var userId = userPrincipal.userId();
    log.info("Getting Tag for user {} with name {}", userId, name);
    var tag = service.getTagResponse(userId, name);

    return ResponseEntity.ok(tag);
  }

  @PostMapping("/tags")
  public ResponseEntity<TagResponse> createTag(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestBody TagRequest request) {
    var userId = userPrincipal.userId();
    log.info("Creating Tag for user {} ", userId);
    TagResponse tag = service.createTag(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(tag);
  }

  @DeleteMapping("/tags/{name}")
  public ResponseEntity<Void> deleteTag(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable String name) {
    var userId = userPrincipal.userId();
    log.info("Deleting Tag for user {} with name {}", userId, name);

    service.deleteTag(userId, name);
    return ResponseEntity.noContent().build();
  }
}
