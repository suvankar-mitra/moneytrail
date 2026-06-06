package cc.suvankar.moneytrail.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.suvankar.moneytrail.auth.dto.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.*;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.junit.jupiter.Container;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserControllerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired ObjectMapper objectMapper;

  private String userToken;

  @BeforeEach
  public void setup() throws Exception {
    User user = new User();
    user.setName("John Doe");
    user.setEmail("john@example.com");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user);

    MvcResult result =
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

    AuthResponse response =
        objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);

    userToken = response.getToken();
    log.debug("Token {}", userToken);
  }

  @Test
  public void getUser_shouldReturn200AndUserResponse_whenValidUserId() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + userToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  public void getUser_shouldReturn401_whenInvalidToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }
}
