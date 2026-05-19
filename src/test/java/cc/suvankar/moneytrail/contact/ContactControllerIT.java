package cc.suvankar.moneytrail.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.suvankar.moneytrail.auth.*;
import cc.suvankar.moneytrail.auth.dto.AuthResponse;
import cc.suvankar.moneytrail.contact.dto.ContactResponse;
import cc.suvankar.moneytrail.user.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContactControllerIT {
  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtUtil jwtUtil;

  private String user1Token;
  private String user2Token;
  private ContactResponse contactResponse1;

  @BeforeAll
  public void setup() throws Exception {
    // USER 1
    var user1 = new User();
    user1.setEmail("john@example.com");
    user1.setName("John Doe");
    user1.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user1);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "email": "john@example.com",
                            "password": "password123"
                        }
                    """))
            .andReturn();

    var authResponse =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AuthResponse.class);
    user1Token = authResponse.getToken();

    String jsonRequest =
        """
            {
              "name": "Contact 1",
              "email": "contact1@example.com",
              "phoneNo": "9988556699",
              "notes": "abcd"
            }
            """;
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/contacts")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andReturn();
    contactResponse1 =
        objectMapper.readValue(result.getResponse().getContentAsString(), ContactResponse.class);

    // USER 2
    var user2 = new User();
    user2.setEmail("jacob@example.com");
    user2.setName("Jacob Doe");
    user2.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user2);
    mvcResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "email": "jacob@example.com",
                            "password": "password123"
                        }
                    """))
            .andReturn();
    authResponse =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AuthResponse.class);
    user2Token = authResponse.getToken();
  }

  @Test
  @Transactional
  public void createContact_ShouldReturn201_whenValidRequest() throws Exception {
    String jsonRequest =
        """
            {
              "name": "Contact 1",
              "email": "contact1@example.com",
              "phoneNo": "9988556699",
              "notes": "abcd"
            }
            """;
    mockMvc
        .perform(
            post("/api/v1/contacts")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Contact 1"))
        .andExpect(jsonPath("$.email").value("contact1@example.com"))
        .andExpect(jsonPath("$.phoneNo").value("9988556699"))
        .andExpect(jsonPath("$.notes").value("abcd"))
        .andExpect(jsonPath("$.contactId").isNotEmpty());
  }

  @Test
  @Transactional
  public void createContact_shouldReturn401_whenInvalidJWT() throws Exception {
    String jsonRequest =
        """
            {
              "name": "Contact 1",
              "email": "contact1@example.com",
              "phoneNo": "9988556699",
              "notes": "abcd"
            }
            """;
    mockMvc
        .perform(
            post("/api/v1/contacts")
                .header("Authorization", "Bearer " + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Transactional
  public void createContact_shouldReturn401_whenUserDoesNotExist() throws Exception {
    String jsonRequest =
        """
            {
              "name": "Contact 1",
              "email": "contact1@example.com",
              "phoneNo": "9988556699",
              "notes": "abcd"
            }
            """;
    String token = jwtUtil.generateTokenWithUserId("random@example.com", UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/v1/contacts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Transactional
  public void getContact_shouldReturn200AndContact_whenValidContactId() throws Exception {
    UUID contactId = contactResponse1.getContactId();

    mockMvc
        .perform(
            get("/api/v1/contacts/" + contactId).header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contactId").value(contactId.toString()));
  }

  @Test
  @Transactional
  public void getContacts_shouldReturn200AndListOfContacts_whenValidUser() throws Exception {
    String jsonRequest =
        """
            {
              "name": "Contact 3",
              "email": "contact3@example.com"
            }
            """;
    mockMvc.perform(
        post("/api/v1/contacts")
            .header("Authorization", "Bearer " + user1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest));

    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/contacts")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andReturn();

    List<ContactResponse> contacts =
        objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(contacts).hasSize(2);
    assertThat(contacts.stream().map(ContactResponse::getName).toList())
        .containsExactlyInAnyOrder("Contact 1", "Contact 3");
    assertThat(contacts.stream().map(ContactResponse::getEmail).toList())
        .containsExactlyInAnyOrder("contact3@example.com", "contact1@example.com");
  }

  @Test
  @Transactional
  public void getContact_shouldReturn404_whenContactDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/contacts/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void getContact_shouldReturn404_whenContactDoesNotBelongToUser() throws Exception {
    UUID contactId = contactResponse1.getContactId();

    mockMvc
        .perform(
            get("/api/v1/contacts/" + contactId).header("Authorization", "Bearer " + user2Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateContact_shouldReturn200AndUpdatedContact_whenValidRequest() throws Exception {
    UUID contactId = contactResponse1.getContactId();

    String jsonRequest =
        """
            {
              "name": "Contact 1 new",
              "email": "contact1@example.com",
              "phoneNo": "9988556699",
              "notes": "abcd"
            }
            """;

    mockMvc
        .perform(
            put("/api/v1/contacts/" + contactId)
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Contact 1 new"))
        .andExpect(jsonPath("$.contactId").value(contactId.toString()));
  }

  @Test
  @Transactional
  public void updateContact_shouldReturn404_whenContactDoesNotBelongToUser() throws Exception {
    UUID contactId = contactResponse1.getContactId();

    String jsonRequest =
        """
            {
              "name": "Contact 1 new",
              "email": "contact1@example.com",
              "phoneNo": "9988556699",
              "notes": "abcd"
            }
            """;

    mockMvc
        .perform(
            put("/api/v1/contacts/" + contactId)
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateContact_shouldReturn404_whenContactIdDoesNotExist() throws Exception {
    String jsonRequest =
        """
            {
              "name": "Contact 1 new",
              "email": "contact1@example.com",
              "phoneNo": "9988556699",
              "notes": "abcd"
            }
            """;

    mockMvc
        .perform(
            put("/api/v1/contacts/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void deleteContact_shouldReturn204_whenValidRequest() throws Exception {
    UUID contactId = contactResponse1.getContactId();

    mockMvc
        .perform(
            delete("/api/v1/contacts/" + contactId).header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNoContent());
  }

  @Test
  @Transactional
  public void deleteContact_shouldReturn404_whenContactDoesNotBelongToUser() throws Exception {
    UUID contactId = contactResponse1.getContactId();

    mockMvc
        .perform(
            delete("/api/v1/contacts/" + contactId).header("Authorization", "Bearer " + user2Token))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void deleteContact_shouldReturn404_whenContactIdDoesNotExist() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/contacts/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + user2Token))
        .andExpect(status().isNotFound());
  }
}
