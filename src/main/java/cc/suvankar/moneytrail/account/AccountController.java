package cc.suvankar.moneytrail.account;

import cc.suvankar.moneytrail.account.dto.AccountRequest;
import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.user.UserPrincipal;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountResponse>> getAccounts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        var userId = userPrincipal.getUserId();

        log.info("Getting all accounts for User {}", userId);

        return ResponseEntity.ok(accountService.getAccounts(userId));
    }

    @PostMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountResponse> createAccount(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @Valid @RequestBody AccountRequest accountRequest) {
        var userId = userPrincipal.getUserId();

        log.info("Creating account for User {}", userId);

        var accountResponse = accountService.createAccount(userId, accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountResponse);
    }

    @GetMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountResponse> getAccount(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                      @PathVariable("id") UUID accountId) {
        var userId = userPrincipal.getUserId();

        log.info("Getting account for User {} with ID {}", userId, accountId);

        return ResponseEntity.ok(accountService.getAccount(userId, accountId));
    }

    @PutMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountResponse> updateAccount(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @PathVariable("id") UUID accountId,
                                                         @Valid @RequestBody AccountRequest accountRequest) {
        var userId = userPrincipal.getUserId();

        log.info("Updating account {} for User {}", accountId, userId);

        var accountResponse = accountService.updateAccount(userId, accountId, accountRequest);
        return ResponseEntity.ok(accountResponse);
    }

    @DeleteMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                              @PathVariable("id") UUID accountId) {
        var userId = userPrincipal.getUserId();

        log.info("Deleting account {} for User {}", accountId, userId);

        accountService.deleteAccount(userId, accountId);

        return ResponseEntity.ok().build();
    }
}
