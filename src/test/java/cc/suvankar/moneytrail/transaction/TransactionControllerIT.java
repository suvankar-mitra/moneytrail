package cc.suvankar.moneytrail.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cc.suvankar.moneytrail.account.*;
import cc.suvankar.moneytrail.account.dto.*;
import cc.suvankar.moneytrail.auth.*;
import cc.suvankar.moneytrail.exchangerate.ExchangeRate;
import cc.suvankar.moneytrail.exchangerate.ExchangeRateId;
import cc.suvankar.moneytrail.exchangerate.ExchangeRateService;
import cc.suvankar.moneytrail.tag.*;
import cc.suvankar.moneytrail.tag.dto.*;
import cc.suvankar.moneytrail.transaction.dto.TransactionRequest;
import cc.suvankar.moneytrail.transaction.dto.TransactionResponse;
import cc.suvankar.moneytrail.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.testcontainers.service.connection.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.*;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.junit.jupiter.Container;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionControllerIT {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AccountService accountService;
  @Autowired private TagService tagService;
  @Autowired private TransactionService transactionService;
  @MockitoBean private ExchangeRateService exchangeRateService;

  private String user1token;
  private String user2token;

  private List<AccountResponse> user1AccountList;
  private List<AccountResponse> user2AccountList;
  private List<TransactionResponse> user1TransactionList;

  private List<TagResponse> user1TagList;

  @BeforeEach
  public void setup() throws Exception {
    // Mock exchange rate
    when(exchangeRateService.getExchangeRate(any(), eq("INR"), eq("USD")))
        .thenReturn(
            new ExchangeRate(
                new ExchangeRateId(), BigDecimal.valueOf(0.001), OffsetDateTime.now(), ""));
    when(exchangeRateService.getExchangeRate(any(), eq("INR"), eq("INR")))
        .thenReturn(
            new ExchangeRate(
                new ExchangeRateId(), BigDecimal.valueOf(1.0), OffsetDateTime.now(), ""));
    when(exchangeRateService.getExchangeRate(any(), eq("USD"), eq("INR")))
        .thenReturn(
            new ExchangeRate(
                new ExchangeRateId(), BigDecimal.valueOf(100.0), OffsetDateTime.now(), ""));

    // USER 1
    var user1 = new User();
    user1.setEmail("john@example.com");
    user1.setName("John Doe");
    user1.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user1);
    user1token = jwtUtil.generateTokenWithUserId(user1.getEmail(), user1.getId());

    // USER 1 Accounts
    user1AccountList = new ArrayList<>();

    user1AccountList.add(
        accountService.createAccount(
            user1.getId(),
            new AccountRequest(null, "Account1INR", AccountType.ASSET, CurrencyCode.INR.name())));
    user1AccountList.add(
        accountService.createAccount(
            user1.getId(),
            new AccountRequest(null, "Account2INR", AccountType.ASSET, CurrencyCode.INR.name())));
    user1AccountList.add(
        accountService.createAccount(
            user1.getId(),
            new AccountRequest(null, "Account3USD", AccountType.ASSET, CurrencyCode.USD.name())));

    // USER 1 TAGS
    user1TagList = new ArrayList<>();
    user1TagList.add(tagService.createTag(user1.getId(), new TagRequest("Food")));
    user1TagList.add(tagService.createTag(user1.getId(), new TagRequest("Grocery")));

    // USER 1 Transactions
    user1TransactionList = new ArrayList<>();
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.getFirst().getAccountId(),
                user1AccountList.get(1).getAccountId(),
                new BigDecimal(100),
                Set.of(user1TagList.getFirst().tagId()),
                LocalDate.of(2026, 01, 8),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(120),
                Set.of(),
                LocalDate.of(2026, 01, 7),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(10),
                Set.of(),
                LocalDate.of(2026, 01, 7),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(12),
                Set.of(),
                LocalDate.of(2026, 01, 6),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(13),
                Set.of(),
                LocalDate.of(2026, 01, 5),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(14),
                Set.of(),
                LocalDate.of(2026, 01, 1),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(14),
                Set.of(),
                LocalDate.of(2026, 01, 1),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(14),
                Set.of(),
                LocalDate.of(2026, 01, 1),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(14),
                Set.of(),
                LocalDate.of(2026, 01, 1),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(14),
                Set.of(),
                LocalDate.of(2026, 01, 1),
                "")));
    user1TransactionList.add(
        transactionService.createTransaction(
            user1.getId(),
            new TransactionRequest(
                user1AccountList.get(1).getAccountId(),
                user1AccountList.get(2).getAccountId(),
                new BigDecimal(14),
                Set.of(),
                LocalDate.of(2026, 01, 1),
                "")));

    // USER 2
    var user2 = new User();
    user2.setEmail("jodi@example.com");
    user2.setName("Jodi Doe");
    user2.setPasswordHash(passwordEncoder.encode("password1234"));
    userRepository.save(user2);
    user2token = jwtUtil.generateTokenWithUserId(user2.getEmail(), user2.getId());

    // USER 2 Accounts
    user2AccountList = new ArrayList<>();

    user2AccountList.add(
        accountService.createAccount(
            user2.getId(),
            new AccountRequest(null, "Account4INR", AccountType.ASSET, CurrencyCode.INR.name())));
    user2AccountList.add(
        accountService.createAccount(
            user2.getId(),
            new AccountRequest(null, "Account5INR", AccountType.ASSET, CurrencyCode.INR.name())));
  }

  @Test
  public void
      createTransaction_shouldReturn201AndTransaction_whenValidRequestInDifferentCurrencies()
          throws Exception {
    String json =
        String.format(
            """
        {
          "fromAccountId": "%s",
          "toAccountId": "%s",
          "transactionAmount": 100,
          "tagIdSet": [],
          "tranDate": "%s",
          "note": ""
        }
        """,
            user1AccountList.getFirst().getAccountId(),
            user1AccountList.get(2).getAccountId(),
            LocalDate.of(2026, 01, 1));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1token)
                .content(json))
        .andExpect(status().isCreated())
        .andExpect(
            jsonPath("$.fromAccount").value(user1AccountList.getFirst().getAccountId().toString()))
        .andExpect(jsonPath("$.toAccount").value(user1AccountList.get(2).getAccountId().toString()))
        .andExpect(jsonPath("$.transactionAmount").value(BigDecimal.valueOf(100)))
        .andExpect(jsonPath("$.effectiveAmount", Matchers.not("100")))
        .andExpect(jsonPath("$.exchangeRate", Matchers.not("1")))
        .andExpect(jsonPath("$.tranDate").value(LocalDate.of(2026, 01, 1).toString()));
  }

  @Test
  public void createTransaction_shouldReturn201AndTransaction_whenValidRequestInSameCurrency()
      throws Exception {
    String json =
        String.format(
            """
        {
          "fromAccountId": "%s",
          "toAccountId": "%s",
          "transactionAmount": 100,
          "tagIdSet": [%d],
          "tranDate": "%s",
          "note": "something"
        }
        """,
            user1AccountList.getFirst().getAccountId(),
            user1AccountList.get(1).getAccountId(),
            user1TagList.getFirst().tagId(),
            LocalDate.of(2026, 01, 12));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1token)
                .content(json))
        .andExpect(status().isCreated())
        .andExpect(
            jsonPath("$.fromAccount").value(user1AccountList.getFirst().getAccountId().toString()))
        .andExpect(jsonPath("$.toAccount").value(user1AccountList.get(1).getAccountId().toString()))
        .andExpect(jsonPath("$.transactionAmount").value("100"))
        .andExpect(jsonPath("$.effectiveAmount").value("100"))
        .andExpect(jsonPath("$.exchangeRate").value("1"))
        .andExpect(jsonPath("$.tranDate").value(LocalDate.of(2026, 01, 12).toString()))
        .andExpect(jsonPath("$.note").value("something"))
        .andExpect(jsonPath("$.tagIdSet").isArray())
        .andExpect(jsonPath("$.tagIdSet", hasSize(1)))
        .andExpect(
            jsonPath("$.tagIdSet", containsInAnyOrder(user1TagList.getFirst().tagId().intValue())));
  }

  @Test
  public void createTransaction_shouldReturn404_whenOneAccountDoesNotBelongToUser()
      throws Exception {
    String json =
        String.format(
            """
        {
          "fromAccountId": "%s",
          "toAccountId": "%s",
          "transactionAmount": 100,
          "tagIdSet": [],
          "tranDate": "%s",
          "note": ""
        }
        """,
            user2AccountList.getFirst().getAccountId(),
            user1AccountList.get(1).getAccountId(),
            LocalDate.of(2026, 01, 12));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user2token)
                .content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createTransaction_shouldReturn404_whenBothAccountsDoNotBelongToUser()
      throws Exception {
    String json =
        String.format(
            """
        {
          "fromAccountId": "%s",
          "toAccountId": "%s",
          "transactionAmount": 100,
          "tagIdSet": [],
          "tranDate": "%s",
          "note": ""
        }
        """,
            user2AccountList.getFirst().getAccountId(),
            user2AccountList.get(1).getAccountId(),
            LocalDate.of(2026, 01, 12));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1token)
                .content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createTransaction_shouldReturn404_whenAccountDoesNotExist() throws Exception {
    String json =
        String.format(
            """
        {
          "fromAccountId": "%s",
          "toAccountId": "%s",
          "transactionAmount": 100,
          "tagIdSet": [],
          "tranDate": "%s",
          "note": ""
        }
        """,
            user1AccountList.getFirst().getAccountId(),
            UUID.randomUUID(),
            LocalDate.of(2026, 01, 12));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1token)
                .content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createTransaction_shouldReturn400_whenTranDateIsInFuture() throws Exception {
    String json =
        String.format(
            """
        {
          "fromAccountId": "%s",
          "toAccountId": "%s",
          "transactionAmount": 100,
          "tagIdSet": [],
          "tranDate": "%s",
          "note": ""
        }
        """,
            user1AccountList.getFirst().getAccountId(),
            user1AccountList.get(1).getAccountId(),
            LocalDate.now().plusDays(3));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1token)
                .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransaction_shouldReturn400_whenAmountIsNegative() throws Exception {
    String json =
        String.format(
            """
        {
          "fromAccountId": "%s",
          "toAccountId": "%s",
          "transactionAmount": -100,
          "tagIdSet": [],
          "tranDate": "%s",
          "note": ""
        }
        """,
            user1AccountList.getFirst().getAccountId(),
            user1AccountList.get(1).getAccountId(),
            LocalDate.of(2026, 01, 12));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1token)
                .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getTransactionById_shouldReturn200AndTransaction_whenValidTransactionId()
      throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/v1/transactions/" + user1TransactionList.getFirst().transactionId())
                    .header("Authorization", "Bearer " + user1token))
            .andExpect(status().isOk())
            .andReturn();

    TransactionResponse response =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), TransactionResponse.class);

    assertThat(response).isEqualTo(user1TransactionList.getFirst());
  }

  @Test
  public void getTransactionById_shouldReturn404_whenTransactionDoesNotBelongToUser()
      throws Exception {

    mockMvc
        .perform(
            get("/api/v1/transactions/" + user1TransactionList.getFirst().transactionId())
                .header("Authorization", "Bearer " + user2token))
        .andExpect(status().isNotFound());
  }

  @Test
  public void updateTransaction_shouldReturn200AndTransaction_whenValidRequest() throws Exception {
    String json =
        String.format(
            """
        {
          "transactionAmount": 200,
          "tagIdSet": [],
          "note": "new note"
        }
        """);

    MvcResult mvcResult =
        mockMvc
            .perform(
                put("/api/v1/transactions/" + user1TransactionList.getFirst().transactionId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .header("Authorization", "Bearer " + user1token))
            .andExpect(status().isOk())
            .andReturn();

    TransactionResponse response =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), TransactionResponse.class);

    assertThat(response.transactionAmount()).isEqualTo(BigDecimal.valueOf(200));
    assertThat(response.effectiveAmount()).isEqualTo(BigDecimal.valueOf(200));
    assertThat(response.tagIdSet()).isEmpty();
    assertThat(response.note()).isEqualTo("new note");
    assertThat(response.fromAccount()).isEqualTo(user1TransactionList.getFirst().fromAccount());
    assertThat(response.toAccount()).isEqualTo(user1TransactionList.getFirst().toAccount());
    assertThat(response.tranDate()).isEqualTo(user1TransactionList.getFirst().tranDate());
  }

  @Test
  public void updateTransaction_shouldReturn404_whenTransactionDoesNotBelongToUser()
      throws Exception {
    String json =
        String.format(
            """
        {
          "transactionAmount": 200,
          "tagIdSet": [],
          "note": "new note"
        }
        """);

    mockMvc
        .perform(
            put("/api/v1/transactions/" + user1TransactionList.getFirst().transactionId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + user2token))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteTransaction_shouldReturn204_whenValidRequest() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/transactions/" + user1TransactionList.getFirst().transactionId())
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/v1/transactions/" + user1TransactionList.getFirst().transactionId())
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteTransaction_shouldReturn404_whenTransactionDoesNotBelongToUser()
      throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/transactions/" + user1TransactionList.getFirst().transactionId())
                .header("Authorization", "Bearer " + user2token))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteTransaction_shouldReturn404_whenInvalidTransactionId() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/transactions/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + user2token))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getTransactions_shouldReturn200AndListOfTransactions_whenValidRequestNoParam()
      throws Exception {
    mockMvc
        .perform(get("/api/v1/transactions").header("Authorization", "Bearer " + user1token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(10)))
        .andExpect(jsonPath("$.page.size").value(10))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(11))
        .andExpect(jsonPath("$.page.totalPages").value(2));
  }

  @Test
  public void getTransactions_shouldReturn200AndListOfTransactions_whenValidRequestWithSize()
      throws Exception {
    mockMvc
        .perform(get("/api/v1/transactions?size=3").header("Authorization", "Bearer " + user1token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(jsonPath("$.page.size").value(3))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(11))
        .andExpect(jsonPath("$.page.totalPages").value(4));
  }

  @Test
  public void getTransactions_shouldReturn200AndListOfTransactions_whenValidRequestWithSizeAndPage()
      throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions?size=3&page=1")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(jsonPath("$.page.size").value(3))
        .andExpect(jsonPath("$.page.number").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(11))
        .andExpect(jsonPath("$.page.totalPages").value(4));
  }

  @Test
  public void
      getTransactions_shouldReturn200AndListOfTransactions_whenValidRequestWithStartDateAndEndDate()
          throws Exception {
    log.info("Transaction list size === {}", user1TransactionList.size());

    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=2026-01-01&endDate=2026-01-05")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(7)))
        .andExpect(jsonPath("$.page.size").value(10))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(7))
        .andExpect(jsonPath("$.page.totalPages").value(1));
  }

  @Test
  public void
      getTransactions_shouldReturn200AndListOfTransactions_whenValidRequestWithStartDateAndEndDateAndSize()
          throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=2026-01-01&endDate=2026-01-05&size=3")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(jsonPath("$.page.size").value(3))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(7))
        .andExpect(jsonPath("$.page.totalPages").value(3));
  }

  @Test
  public void
      getTransactions_shouldReturn200AndListOfTransactions_whenValidRequestWithStartDateAndEndDateAndSizeAndPage()
          throws Exception {
    log.info("Transaction list size === {}", user1TransactionList.size());

    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=2026-01-01&endDate=2026-01-05&size=3&page=2")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.page.size").value(3))
        .andExpect(jsonPath("$.page.number").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(7))
        .andExpect(jsonPath("$.page.totalPages").value(3));
  }

  @Test
  public void
      getTransactions_shouldReturn200AndListOfTransactions_whenValidRequestWithStartDateAndEndDateAndSizeAndPageAndSort()
          throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=2026-01-01&endDate=2026-01-05&size=3&page=2&sort=tranDate")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.page.size").value(3))
        .andExpect(jsonPath("$.page.number").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(7))
        .andExpect(jsonPath("$.page.totalPages").value(3));
  }

  @Test
  public void
      getTransactions_shouldReturn400AndListOfTransactions_whenInvalidRequestWithStartDateIsNullAndEndDateIsValid()
          throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=null&endDate=2026-01-05")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getTransactions_shouldReturn400AndListOfTransactions_whenInvalidRequestWithStartDateIsValidAndEndDateIsNull()
          throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=2026-01-05&endDate=null")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getTransactions_shouldReturn400AndListOfTransactions_whenInvalidRequestWithStartDateIsNullAndEndDateIsNull()
          throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=null&endDate=null")
                .header("Authorization", "Bearer " + user1token))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getTransactions_shouldReturn401_whenInvalidToken() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions?startDate=null&endDate=null")
                .header("Authorization", "Bearer " + UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void getTransactions_shouldReturn200AndEmptyList_whenUserDoesNotHaveTransaction()
      throws Exception {
    mockMvc
        .perform(get("/api/v1/transactions").header("Authorization", "Bearer " + user2token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(0)))
        .andExpect(jsonPath("$.page.size").value(10))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0))
        .andExpect(jsonPath("$.page.totalPages").value(0));
  }
}
