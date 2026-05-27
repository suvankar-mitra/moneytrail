package cc.suvankar.moneytrail.user;

import cc.suvankar.moneytrail.user.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class UserController {
  private UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping("/users/me")
  public ResponseEntity<UserResponse> getUser(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal) {
    var userId = userPrincipal.userId();
    log.info("Getting user's profile for {}", userId);
    return ResponseEntity.ok(service.getUserResponseById(userId));
  }
}
