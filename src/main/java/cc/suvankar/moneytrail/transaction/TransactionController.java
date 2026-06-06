package cc.suvankar.moneytrail.transaction;

import cc.suvankar.moneytrail.transaction.dto.TransactionQueryFilter;
import cc.suvankar.moneytrail.transaction.dto.TransactionRequest;
import cc.suvankar.moneytrail.transaction.dto.TransactionResponse;
import cc.suvankar.moneytrail.transaction.dto.TransactionUpdateRequest;
import cc.suvankar.moneytrail.user.UserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class TransactionController {
  private final TransactionService service;

  public TransactionController(TransactionService service) {
    this.service = service;
  }

  @PostMapping("/transactions")
  public ResponseEntity<TransactionResponse> createTransaction(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @Valid @RequestBody TransactionRequest request) {
    UUID userId = userPrincipal.userId();

    log.info("Creating new transaction for User {}", userId);

    var response = service.createTransaction(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/transactions/{id}")
  public ResponseEntity<TransactionResponse> getTransactionById(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @NonNull @Valid @PathVariable UUID id) {
    UUID userId = userPrincipal.userId();

    log.info("Getting one transaction for User {}", userId);

    var response = service.getTransaction(userId, id);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/transactions/{id}")
  public ResponseEntity<TransactionResponse> updateTransaction(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @NonNull @Valid @PathVariable UUID id,
      @NonNull @Valid @RequestBody TransactionUpdateRequest request) {
    UUID userId = userPrincipal.userId();

    log.info("Updating transaction for User {}", userId);

    var response = service.updateTransaction(userId, id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/transactions/{id}")
  public ResponseEntity<TransactionResponse> deleteTransaction(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @NonNull @Valid @PathVariable UUID id) {
    UUID userId = userPrincipal.userId();

    log.info("Deleting transaction for User {}", userId);

    service.deleteTransaction(userId, id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/transactions")
  public ResponseEntity<Page<TransactionResponse>> getTransactions(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(name = "startDate", required = false) LocalDate startDate,
      @RequestParam(name = "endDate", required = false) LocalDate endDate,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    UUID userId = userPrincipal.userId();

    log.info("Getting transactions for User {}", userId);

    TransactionQueryFilter filter = new TransactionQueryFilter(startDate, endDate);

    var response = service.getTransactions(userId, filter, pageable);
    return ResponseEntity.ok(response);
  }
}
