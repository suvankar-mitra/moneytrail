# MoneyTrail — Architecture Document

> **Version:** v0.0.1  
> **Stack:** Java 21 · Spring Boot 3 · PostgreSQL · Flyway · JWT · React (frontend, separate)  
> **Last updated:** 2026-03

---

## Table of Contents

1. [Project Overview & Goals](#1-project-overview--goals)
2. [Folder & Package Structure](#2-folder--package-structure)
3. [Layering Rules](#3-layering-rules)
4. [Domain Model Summary](#4-domain-model-summary)
5. [API Design & DTO Patterns](#5-api-design--dto-patterns)
6. [Authentication — JWT](#6-authentication--jwt)
7. [Flyway Migration Strategy](#7-flyway-migration-strategy)
8. [Phased Build Order](#8-phased-build-order)

---

## 1. Project Overview & Goals

MoneyTrail is a personal finance tracking application. Its core purpose is to give a single user (with future multi-user
support) a clear, structured view of where their money comes from, where it goes, and what they owe or are owed.

The application is built as a **pure REST API backend** from day one. This is a deliberate architectural decision: by
keeping the backend completely decoupled from any UI, the same API can serve a React web app, a Flutter mobile app, or
any other client in the future without touching the server code.

**The central design principle is double-entry bookkeeping.** Every financial event is modelled as money moving
*between* two accounts — a `from_account` and a `to_account`. There are no special-case transaction types (no separate "
expense" or "income" records). This single model handles everything: paying a bill, receiving a salary, lending money to
a friend, investing in stocks. This keeps the domain clean and extensible.

---

## 2. Folder & Package Structure

The project is organised **by domain**, not by layer. This means all classes related to a concept (entity, repository,
service, controller, DTOs) live together under one package, rather than having a global `controllers/` folder containing
every controller in the app.

This approach makes it easy to reason about a feature in isolation, and scales well as the app grows.

```
com.moneytrail
│
├── user/
│   ├── User.java                  (entity)
│   ├── UserRepository.java
│   ├── UserService.java
│   ├── UserController.java
│   └── dto/
│       ├── UserRequest.java
│       └── UserResponse.java
│
├── contact/
│   ├── Contact.java
│   ├── ContactRepository.java
│   ├── ContactService.java
│   ├── ContactController.java
│   └── dto/
│
├── account/
│   ├── Account.java
│   ├── AccountType.java           (enum: ASSET, LIABILITY, INCOME, etc.)
│   ├── AccountRepository.java
│   ├── AccountService.java
│   ├── AccountController.java
│   └── dto/
│
├── transaction/
│   ├── Transaction.java
│   ├── TransactionRepository.java
│   ├── TransactionService.java
│   ├── TransactionController.java
│   └── dto/
│
├── tag/
│   ├── Tag.java
│   ├── TransactionTag.java
│   ├── TagRepository.java
│   ├── TagService.java
│   ├── TagController.java
│   └── dto/
│
├── dashboard/
│   ├── DashboardService.java      (aggregates data across domains)
│   └── DashboardController.java
│
├── auth/
│   ├── AuthController.java        (login, register endpoints)
│   ├── AuthService.java
│   ├── JwtUtil.java
│   └── dto/
│
└── config/
    ├── SecurityConfig.java
    └── JwtFilter.java
```

> **Why `dashboard/` is separate:** The dashboard cuts across accounts, transactions, and tags to produce summaries. It
> doesn't own any data — it reads from other domains. Keeping it separate prevents it from polluting any single domain
> package.

---

## 3. Layering Rules

Every domain follows a strict, unidirectional dependency chain:

```
Controller → Service → Repository → Entity
```

Each layer has one job:

| Layer          | Responsibility                                                                                               | What it must NOT do                                          |
|----------------|--------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| **Controller** | Parse HTTP request, call service, return HTTP response                                                       | Contain business logic, access repository directly           |
| **Service**    | Enforce business rules, orchestrates its own repository and interacts with other domains via their services. | Know about HTTP (no `HttpServletRequest`, no response codes) |
| **Repository** | Talk to the database (JPA/JDBC queries)                                                                      | Contain business logic, call other services                  |
| **Entity**     | Represent the database table as a Java object                                                                | Contain business logic, be sent directly over the wire       |

**DTOs live at the boundary.** Controllers receive `*Request` DTOs from the client and return `*Response` DTOs to the
client. Entities never leave the service layer. This means the database schema and the API contract can evolve
independently — a critical separation as the app grows.

```
HTTP Request
    ↓
Controller  (receives UserRequest DTO)
    ↓
Service     (works with Entity internally)
    ↓
Repository  (persists/fetches Entity)
    ↓
Database

    ↑
Controller  (returns UserResponse DTO)
    ↑
HTTP Response
```

---

## 4. Domain Model Summary

The schema has six tables. See `docs/erd/moneytrail_erd.html` for the full visual ERD.

### users

The root of all data. Every piece of data in the system belongs to a user via a `user_id` foreign key. Designed for
future multi-user support from day one, even though v0.0.1 is single-user.

### contacts

External people — friends, family, colleagues — that a user interacts with financially. A contact has no login; they are
just a reference entity used in lending/borrowing scenarios. Linked to a user via `user_id`.

### accounts

The most important concept in the system. An account is any financial "bucket" — a bank account, a credit card, a
category for groceries, a salary income source, a loan you gave a friend. The `type` field (ASSET, LIABILITY, INCOME,
EXPENSE, INVESTMENT, RECEIVABLE, PAYABLE) classifies it.

The optional `contact_id` links an account to a person — this is what makes RECEIVABLE and PAYABLE accounts
meaningful ("John owes me ₹2,000" is modelled as a RECEIVABLE account linked to John's contact record).

### transactions

The core financial event. A transaction always has a `from_account_id` and a `to_account_id` — money moves from one
account to another. This is the double-entry principle. Both FK references use `ON DELETE RESTRICT`, meaning you cannot
delete an account that has transaction history.

**Examples of how the model works in practice:**

| Scenario             | from_account                   | to_account                     |
|----------------------|--------------------------------|--------------------------------|
| Paid grocery bill    | Bank (ASSET)                   | Groceries (EXPENSE)            |
| Received salary      | Employer (INCOME)              | Bank (ASSET)                   |
| Lent money to friend | Bank (ASSET)                   | Friend receivable (RECEIVABLE) |
| Friend repaid        | Friend receivable (RECEIVABLE) | Bank (ASSET)                   |

### tags & transaction_tags

Tags allow flexible, user-defined categorisation of transactions (e.g. "vacation", "work", "Q1"). `transaction_tags` is
a pure join table — a transaction can have many tags, a tag can apply to many transactions. The
`UNIQUE(transaction_id, tag_id)` constraint prevents duplicates at the database level.

---

## 5. API Design & DTO Patterns

The API is a standard REST API. Resources map to domains. All endpoints are prefixed with `/api/v1/`.

**Planned endpoints (v0.0.1):**

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login

GET    /api/v1/accounts
POST   /api/v1/accounts
GET    /api/v1/accounts/{id}
PUT    /api/v1/accounts/{id}
DELETE /api/v1/accounts/{id}

GET    /api/v1/transactions
POST   /api/v1/transactions
GET    /api/v1/transactions/{id}
PUT    /api/v1/transactions/{id}
DELETE /api/v1/transactions/{id}

GET    /api/v1/contacts
POST   /api/v1/contacts
GET    /api/v1/contacts/{id}
PUT    /api/v1/contacts/{id}
DELETE /api/v1/contacts/{id}

GET    /api/v1/tags
POST   /api/v1/tags
DELETE /api/v1/tags/{id}

GET    /api/v1/dashboard/summary
```

**DTO conventions:**

- `*Request` — inbound payload from client. Contains only fields the client is allowed to set. Annotated with `@Valid`
  constraints (`@NotNull`, `@NotBlank`, `@Positive`, etc.).
- `*Response` — outbound payload to client. Shaped for what the client needs to display. Never exposes sensitive
  fields (e.g. `password_hash`).
- Mapping between Entity ↔ DTO is done inside the Service layer (or a dedicated mapper class per domain).

```java
// Example: Controller receives Request, returns Response
@PostMapping
public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
    AccountResponse response = accountService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

## 6. Authentication — JWT

Authentication uses **JSON Web Tokens (JWT)**. The flow is stateless — the server does not store sessions.

**Flow:**

1. Client sends credentials to `POST /api/v1/auth/login`.
2. Server validates credentials, signs a JWT with a secret key, and returns the token.
3. Client stores the token (in memory or localStorage) and sends it in every subsequent request as a header:
   `Authorization: Bearer <token>`.
4. A `JwtFilter` (Spring `OncePerRequestFilter`) intercepts every request, extracts and validates the token, and sets
   the authenticated user in the `SecurityContext`.
5. Controllers and services can then call `SecurityContextHolder` to know which user is making the request — and scope
   all queries to that user's data.

**Key classes:**

- `JwtUtil` — signs and parses tokens. Holds the secret key and expiry config.
- `JwtFilter` — the filter that validates tokens on every request.
- `SecurityConfig` — configures Spring Security: which endpoints are public (auth routes), which require
  authentication (everything else), and registers the `JwtFilter`.

**Why JWT and not sessions?** Because the frontend (React) and backend are separate services. JWT is the natural fit for
a stateless API that will be consumed by multiple clients (web, mobile).

---

## 7. Flyway Migration Strategy

Database schema changes are managed entirely through **Flyway**. No DDL is ever run manually against the database. This
makes the schema auditable, repeatable, and safe to run in any environment.

**File naming convention:**

```
src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__create_contacts.sql
├── V3__create_accounts.sql
├── V4__create_transactions.sql
├── V5__create_tags.sql
└── V6__create_transaction_tags.sql
```

The pattern is `V{version}__{description}.sql` — two underscores between version and description. Flyway runs these in
order on application startup and tracks which have been applied in its `flyway_schema_history` table.

**Rules:**

- **Never edit a migration file that has already been committed and run.** Flyway checksums each file — modifying it
  will cause a startup failure. If you need to change something, write a new migration (e.g.
  `V7__add_balance_to_accounts.sql`).
- One migration file per logical change. Do not bundle unrelated changes together.
- Migration files are the source of truth for the schema. The JPA entities should reflect what Flyway creates — not the
  other way around. Hibernate's `spring.jpa.hibernate.ddl-auto` is set to `validate` (not `create` or `update`), so
  Spring will fail fast if the entities don't match the database.

---

## 8. Phased Build Order

The project is built in phases to keep each step focused and testable before moving to the next.

**Phase 1 — Foundation**

- Flyway migrations for all 6 tables
- JPA entities for all 6 domains
- Spring Security + JWT setup (register, login)
- Confirm the app starts, connects to the database, and auth endpoints work

**Phase 2 — Core CRUD**

- Account endpoints (CRUD)
- Contact endpoints (CRUD)
- Tag endpoints (CRUD)
- All scoped to the authenticated user

**Phase 3 — Transactions**

- Transaction endpoints (CRUD)
- Validate that `from_account` and `to_account` both belong to the authenticated user
- Transaction tagging (add/remove tags on a transaction)

**Phase 4 — Dashboard**

- Summary endpoint: total balance per account, spending by tag, recent transactions
- This phase is read-only — it aggregates data already stored

**Phase 5 — React Frontend**

- Begin React app as a separate project
- Consumes the REST API from Phase 1–4
- Auth flow (login/register), account list, transaction list, dashboard view

**Post v0.0.1 (future)**

- Multi-user linking (shared accounts, group expenses)
- Inter-user transactions
- Budgeting and alerts

---

*This document should be updated whenever a significant architectural decision is made or the domain model changes.*