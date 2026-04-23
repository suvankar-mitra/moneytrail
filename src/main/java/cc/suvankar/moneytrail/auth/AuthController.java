package cc.suvankar.moneytrail.auth;

import cc.suvankar.moneytrail.auth.dto.AuthResponse;
import cc.suvankar.moneytrail.auth.dto.LoginRequest;
import cc.suvankar.moneytrail.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
    authService.registerUser(registerRequest);
    log.info("User with email {} is registered.", registerRequest.getEmail());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    var authResponse = authService.loginUser(loginRequest);
    log.info("Authentication successful for user with email {}", loginRequest.getEmail());
    return ResponseEntity.ok(authResponse);
  }
}
