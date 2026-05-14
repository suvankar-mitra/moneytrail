# MoneyTrail — Progress Tracker
> Last updated: 2026-03 · Stack: Java 21 · Spring Boot 3 · PostgreSQL · Flyway · JWT · React

---

## 🏗️ Phase 1 — Foundation

### Flyway Migrations
- [x] `V1__create_users.sql`
- [x] `V2__create_contacts.sql`
- [x] `V3__create_accounts.sql`
- [x] `V4__create_transactions.sql`
- [x] `V5__create_tags.sql`
- [x] `V6__create_transaction_tags.sql`

### JPA Entities
- [x] `User.java`
- [x] `Contact.java`
- [x] `Account.java` + `AccountType.java` (enum)
- [x] `Transaction.java`
- [x] `Tag.java`
- [x] `TransactionTag.java` + `TransactionTagId.java` (composite key)

### Authentication (JWT)
- [x] `JwtUtil.java` — signs & parses tokens
- [x] `JwtFilter.java` — validates token on every request
- [x] `SecurityConfig.java` — public vs protected routes, filter registration
- [x] `AuthService.java` — register & login business logic
- [x] `AuthController.java` — `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
- [x] `UserPrincipal.java` — custom `UserDetails` holding email + UUID (eliminates per-request DB lookup)
- [x] `GlobalExceptionHandler.java` — handles auth & validation errors, no PII in responses

### Config
- [x] `AppConfig.java`
- [x] `JpaConfig.java`
- [x] `SecurityConfig.java`
- [x] `application.yml` (shared config)
- [x] `application-dev.yml` (gitignored, H2/secrets)
- [x] `application-test-h2.yml` (test profile)

### Exception Layer
- [x] `ResourceNotFoundException.java` (with static factory methods: `forAccount()`, `forContact()`, `forUser()`)
- [x] `BadRequestException.java`
- [x] `EmailAlreadyExistsException.java`
- [x] `InvalidCredentialsException.java`
- [x] `ErrorResponse.java`
- [x] `GlobalExceptionHandler.java`

### Documentation
- [x] `architecture.md`
- [x] `moneytrail_erd.html`

---

## 📦 Phase 2 — Core CRUD

### Accounts
- [x] `Account.java` entity
- [x] `AccountRepository.java`
- [x] `AccountService.java`
- [x] `AccountController.java` — full CRUD (`GET`, `POST`, `PUT`, `DELETE`)
- [x] `AccountRequest.java` / `AccountResponse.java` DTOs
- [x] IDOR protection (all queries scoped to `userId` from JWT; returns 404 not 403)
- [x] RECEIVABLE/PAYABLE validation (requires non-null `contactId`)

### Contacts
- [x] `Contact.java` entity
- [x] `ContactRepository.java`
- [x] `ContactService.java`
- [x] `ContactController.java` — full CRUD
- [x] `ContactRequest.java` / `ContactResponse.java` DTOs
- [ ] `ContactControllerIT.java` — integration tests (happy + sad paths)

### Tags
- [x] `Tag.java` entity
- [x] `TagRepository.java`
- [ ] `TagService.java`
- [ ] `TagController.java` — `GET /api/v1/tags`, `POST /api/v1/tags`, `DELETE /api/v1/tags/{id}`
- [ ] `TagRequest.java` / `TagResponse.java` DTOs
- [ ] `TagControllerIT.java` — integration tests

---

## 💸 Phase 3 — Transactions

- [x] `Transaction.java` entity
- [x] `TransactionRepository.java`
- [ ] `TransactionService.java`
- [ ] `TransactionController.java` — full CRUD (`GET`, `POST`, `PUT`, `DELETE`)
- [ ] `TransactionRequest.java` / `TransactionResponse.java` DTOs
- [ ] Validate `from_account` and `to_account` both belong to the authenticated user
- [ ] Transaction tagging — add/remove tags on a transaction
- [ ] `TransactionControllerIT.java` — integration tests

---

## 📊 Phase 4 — Dashboard

- [ ] `DashboardService.java` — aggregates across accounts, transactions, tags
- [ ] `DashboardController.java` — `GET /api/v1/dashboard/summary`
- [ ] Summary: total balance per account
- [ ] Summary: spending by tag
- [ ] Summary: recent transactions
- [ ] Integration tests for dashboard endpoint

---

## ⚛️ Phase 5 — React Frontend

- [ ] Scaffold React app (separate project)
- [ ] Auth flow — login & register pages
- [ ] Account list view
- [ ] Transaction list view
- [ ] Dashboard view
- [ ] API integration (consuming v1 REST API)

---

## 🧪 Testing

### Repository Tests (`@DataJpaTest`)
- [x] `UserRepositoryTest.java`
- [x] `AccountRepositoryTest.java`
- [x] `ContactRepositoryTest.java`
- [x] `TagRepositoryTest.java`
- [x] `TransactionRepositoryTest.java`

### Controller Unit Tests (`@WebMvcTest`)
- [x] `AuthControllerTest.java`

### Integration Tests (`@SpringBootTest` + `MockMvc` + H2)
- [x] `AuthControllerIT.java` — happy + sad paths
- [x] `AccountControllerIT.java` — happy + sad paths
- [ ] `ContactControllerIT.java`
- [ ] `TagControllerIT.java`
- [ ] `TransactionControllerIT.java`
- [ ] `DashboardControllerIT.java`

---

## 🔮 Post v0.0.1 (Future)

- [ ] Multi-user linking (shared accounts, group expenses)
- [ ] Inter-user transactions
- [ ] Budgeting and alerts
- [ ] Flutter mobile app client

