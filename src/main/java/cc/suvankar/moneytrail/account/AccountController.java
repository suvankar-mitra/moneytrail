package cc.suvankar.moneytrail.account;

import cc.suvankar.moneytrail.account.dto.AccountRequest;
import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.user.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.websocket.server.*;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    var userId = userPrincipal.userId();

    log.info("Getting all accounts for User {}", userId);

    return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
  }

  @GetMapping(value = "/accounts/opening_balance_equity")
  public ResponseEntity<AccountResponse> getOpeningBalanceEquityAccountByCurrency(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam("currency") String currency) {
    var userId = userPrincipal.userId();

    log.info("Getting OPENING_BALANCE_EQUITY({}) accounts for User {}", currency, userId);

    return ResponseEntity.ok(
        accountService.getOpeningBalanceEquityAccountByCurrency(userId, currency));
  }

  @PostMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccountResponse> createAccount(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @Valid @RequestBody AccountRequest accountRequest) {
    var userId = userPrincipal.userId();

    log.info("Creating account for User {}", userId);

    var accountResponse = accountService.createAccount(userId, accountRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
  }

  @GetMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccountResponse> getAccount(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable("id") UUID accountId) {
    var userId = userPrincipal.userId();

    log.info("Getting account for User {} with ID {}", userId, accountId);

    return ResponseEntity.ok(accountService.getAccount(userId, accountId));
  }

  @PutMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccountResponse> updateAccount(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable("id") UUID accountId,
      @NonNull @Valid @RequestBody AccountRequest accountRequest) {
    var userId = userPrincipal.userId();

    log.info("Updating account {} for User {}", accountId, userId);

    var accountResponse = accountService.updateAccount(userId, accountId, accountRequest);
    return ResponseEntity.ok(accountResponse);
  }

  @DeleteMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteAccount(
      @NonNull @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @PathVariable("id") UUID accountId) {
    var userId = userPrincipal.userId();

    log.info("Deleting account {} for User {}", accountId, userId);

    accountService.deleteAccount(userId, accountId);

    return ResponseEntity.ok().build();
  }
}
