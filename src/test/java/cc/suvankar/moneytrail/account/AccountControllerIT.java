package cc.suvankar.moneytrail.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.suvankar.moneytrail.account.dto.AccountResponse;
import cc.suvankar.moneytrail.auth.dto.AuthResponse;
import cc.suvankar.moneytrail.contact.Contact;
import cc.suvankar.moneytrail.contact.ContactRepository;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountControllerIT {

  @Autowired private UserRepository userRepository;
  @Autowired private ContactRepository contactRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;

  private String user1Token;
  private String user2Token;
  private UUID user1ContactId;
  private List<AccountResponse> accountResponseList;

  @BeforeAll
  public void setup() throws Exception {
    log.info("Setting up Before all");

    // USER 1
    var user1 = new User();
    user1.setEmail("john@example.com");
    user1.setName("John Doe");
    user1.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user1);

    Contact contactForUser1 = new Contact();
    contactForUser1.setUser(user1);
    contactForUser1.setEmail("contact@example.com");
    contactForUser1.setName("Contact For User1");
    contactRepository.save(contactForUser1);
    user1ContactId = contactForUser1.getId();

    String validJsonRequest =
        """
                {
                    "email": "john@example.com",
                    "password": "password123"
                }
                """;

    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJsonRequest))
            .andReturn();

    var authResponse =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AuthResponse.class);
    user1Token = authResponse.getToken();

    // Accounts for USER 1
    accountResponseList = new ArrayList<>();

    String validJson1 =
        """
                {
                  "contactId": null,
                  "name": "Primary Savings Account",
                  "accountType": "ASSET",
                  "currency": "INR",
                  "virtual": false
                }
                """;
    mvcResult =
        mockMvc
            .perform(
                post("/api/v1/accounts")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson1))
            .andReturn();
    accountResponseList.add(
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), AccountResponse.class));

    String validJson2 =
        """
                {
                  "contactId": null,
                  "name": "Secondary Savings Account",
                  "accountType": "ASSET",
                  "currency": "INR",
                  "virtual": false
                }
                """;
    mvcResult =
        mockMvc
            .perform(
                post("/api/v1/accounts")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson2))
            .andReturn();
    accountResponseList.add(
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), AccountResponse.class));

    // USER 2
    var user2 = new User();
    user2.setEmail("jacob@example.com");
    user2.setName("Jacob Doe");
    user2.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user2);

    validJsonRequest =
        """
                {
                    "email": "jacob@example.com",
                    "password": "password123"
                }
                """;

    mvcResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJsonRequest))
            .andReturn();

    authResponse =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AuthResponse.class);
    user2Token = authResponse.getToken();

    // Accounts for USER 2
    String validJson =
        """
                {
                  "contactId": null,
                  "name": "Bank Account",
                  "accountType": "ASSET",
                  "currency": "INR",
                  "virtual": false
                }
                """;
    mockMvc.perform(
        post("/api/v1/accounts")
            .header("Authorization", "Bearer " + user2Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson));

    log.info("Done setting up Before all");
  }

  @Test
  @Transactional
  public void createAccount_shouldReturn201_whenValidRequest() throws Exception {
    String validJson =
        """
                {
                  "contactId": "%s",
                  "name": "Primary Savings Account",
                  "accountType": "PAYABLE",
                  "currency": "INR",
                  "virtual": false
                }
                """
            .formatted(user1ContactId.toString());
    mockMvc
        .perform(
            post("/api/v1/accounts")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accountId").isString())
        .andExpect(jsonPath("$.contactId").value(user1ContactId.toString()))
        .andExpect(jsonPath("$.name").value("Primary Savings Account"))
        .andExpect(jsonPath("$.accountType").value("PAYABLE"))
        .andExpect(jsonPath("$.currency").value("INR"));
  }

  @Test
  @Transactional
  public void createAccount_shouldFail_whenContactDoesNotExist() throws Exception {
    String json =
        """
                {
                  "contactId": "%s",
                  "name": "Test",
                  "accountType": "PAYABLE",
                  "currency": "INR",
                  "virtual": false
                }
                """
            .formatted(UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/v1/accounts")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void getAccountById_shouldReturn200AndAccount_whenValidAccountId() throws Exception {
    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(accountId.toString()));
  }

  @Test
  @Transactional
  public void getAccounts_shouldReturn200AndListOfAccounts_whenValidUser() throws Exception {

    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + user1Token))
            .andExpect(status().isOk())
            .andReturn();

    List<AccountResponse> listOfObjects =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(listOfObjects).hasSize(2);
    assertThat(listOfObjects.stream().map(AccountResponse::getName).toList())
        .containsExactlyInAnyOrderElementsOf(
            accountResponseList.stream().map(AccountResponse::getName).toList());
    assertThat(listOfObjects.stream().map(AccountResponse::getAccountId).toList())
        .containsExactlyInAnyOrderElementsOf(
            accountResponseList.stream().map(AccountResponse::getAccountId).toList());
  }

  @Test
  @Transactional
  public void getAccount_shouldReturn401_whenInvalidJWT() throws Exception {
    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1Token + " "))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Transactional
  public void getAccount_shouldReturn404_whenAccountIdDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/accounts/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void getAccount_shouldReturn404_whenAccountDoesNotBelongToUser() throws Exception {
    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + user2Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateAccount_shouldReturn200AndUpdateAccount_whenValidRequest() throws Exception {
    String validJson =
        """
                {
                  "contactId": "%s",
                  "name": "Contact's account",
                  "accountType": "PAYABLE",
                  "currency": "INR",
                  "virtual": false
                }
                """
            .formatted(user1ContactId);

    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            put("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson)
                .header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(accountId.toString()))
        .andExpect(jsonPath("$.name").value("Contact's account"));
  }

  @Test
  @Transactional
  public void updateAccount_shouldReturn401_whenInvalidJWT() throws Exception {
    String validJson =
        """
                {
                  "contactId": "%s",
                  "name": "Contact's account",
                  "accountType": "PAYABLE",
                  "currency": "INR",
                  "virtual": false
                }
                """
            .formatted(user1ContactId);

    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            put("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson)
                .header("Authorization", "Bearer " + user1Token + " "))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Transactional
  public void updateAccount_shouldReturn404_whenAccountIdDoesNotExist() throws Exception {
    String validJson =
        """
                {
                  "contactId": null,
                  "name": "Contact's account",
                  "accountType": "ASSET",
                  "currency": "INR",
                  "virtual": false
                }
                """;

    mockMvc
        .perform(
            put("/api/v1/accounts/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson)
                .header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateAccount_shouldReturn404_whenAccountDoesNotBelongToUser() throws Exception {
    String validJson =
        """
                {
                  "contactId": null,
                  "name": "Contact's account",
                  "accountType": "ASSET",
                  "currency": "INR",
                  "virtual": false
                }
                """;
    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            put("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson)
                .header("Authorization", "Bearer " + user2Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void deleteAccount_shouldReturn200_whenValidRequest() throws Exception {
    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            delete("/api/v1/accounts/" + accountId).header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/v1/accounts/" + accountId).header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void deleteAccount_shouldReturn401_whenInvalidJWT() throws Exception {
    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            delete("/api/v1/accounts/" + accountId)
                .header("Authorization", "Bearer " + user1Token + " "))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Transactional
  public void deleteAccount_shouldReturn404_whenAccountIdDoesNotExist() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/accounts/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void deleteAccount_shouldReturn404_whenAccountIdDoesNotBelongToUser() throws Exception {
    UUID accountId = accountResponseList.getFirst().getAccountId();

    mockMvc
        .perform(
            delete("/api/v1/accounts/" + accountId).header("Authorization", "Bearer " + user2Token))
        .andExpect(status().isNotFound());
  }
}
