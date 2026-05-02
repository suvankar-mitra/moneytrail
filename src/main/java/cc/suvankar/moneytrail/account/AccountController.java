package cc.suvankar.moneytrail.account;

import cc.suvankar.moneytrail.account.dto.AccountRequest;
import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.user.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AccountResponse>> getAccounts(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal) {
    var userId = userPrincipal.getUserId();

    log.info("Getting all accounts for User {}", userId);

    return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
  }

  @PostMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccountResponse> createAccount(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @Valid @RequestBody AccountRequest accountRequest) {
    var userId = userPrincipal.getUserId();

    log.info("Creating account for User {}", userId);

    var accountResponse = accountService.createAccount(userId, accountRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
  }

  @GetMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccountResponse> getAccount(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable("id") UUID accountId) {
    var userId = userPrincipal.getUserId();

    log.info("Getting account for User {} with ID {}", userId, accountId);

    return ResponseEntity.ok(accountService.getAccount(userId, accountId));
  }

  @PutMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccountResponse> updateAccount(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable("id") UUID accountId,
      @NonNull @Valid @RequestBody AccountRequest accountRequest) {
    var userId = userPrincipal.getUserId();

    log.info("Updating account {} for User {}", accountId, userId);

    var accountResponse = accountService.updateAccount(userId, accountId, accountRequest);
    return ResponseEntity.ok(accountResponse);
  }

  @DeleteMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteAccount(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable("id") UUID accountId) {
    var userId = userPrincipal.getUserId();

    log.info("Deleting account {} for User {}", accountId, userId);

    accountService.deleteAccount(userId, accountId);

    return ResponseEntity.ok().build();
  }
}
