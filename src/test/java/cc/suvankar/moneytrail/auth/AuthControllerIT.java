package cc.suvankar.moneytrail.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.suvankar.moneytrail.auth.dto.AuthResponse;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@Transactional
public class AuthControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private RefreshTokenRepository refreshTokenRepository;

  private static final String registerBaseUrl = "/api/v1/auth/register";
  private static final String loginBaseUrl = "/api/v1/auth/login";

  @Test
  public void register_shouldReturn201_whenValidRequest() throws Exception {
    String validJsonRequest =
        """
                {
                    "name": "John Doe",
                    "email": "john@example.com",
                    "password": "password123"
                }
                """;

    mockMvc
        .perform(
            post(registerBaseUrl).contentType(MediaType.APPLICATION_JSON).content(validJsonRequest))
        .andExpect(status().isCreated());
  }

  @Test
  public void register_shouldReturn400_whenEmailIsMalformed() throws Exception {
    String invalidJsonRequest =
        """
                {
                    "name": "John Doe",
                    "email": "john#exampe_com",
                    "password": "password123"
                }
                """;

    mockMvc
        .perform(
            post(registerBaseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isString())
        .andExpect(jsonPath("$.message").value("email: Must be a valid email address"));
  }

  @Test
  public void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
    var user = new User();
    user.setEmail("john@example.com");
    user.setName("Johny Doe");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user);

    String dupEmailJsonRequest =
        """
                {
                    "name": "John Doe",
                    "email": "john@example.com",
                    "password": "password123"
                }
                """;

    mockMvc
        .perform(
            post(registerBaseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dupEmailJsonRequest))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void login_shouldReturn200AndJwtToken_whenValidCredential() throws Exception {
    var user = new User();
    user.setEmail("john@example.com");
    user.setName("John Doe");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user);

    String validJsonRequest =
        """
                {
                    "email": "john@example.com",
                    "password": "password123"
                }
                """;

    mockMvc
        .perform(
            post(loginBaseUrl).contentType(MediaType.APPLICATION_JSON).content(validJsonRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists());
  }

  @Test
  public void login_shouldReturn401_whenIncorrectCredential() throws Exception {
    var user = new User();
    user.setEmail("john@example.com");
    user.setName("John Doe");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user);

    String validJsonRequest =
        """
                {
                    "email": "john@example.com",
                    "password": "password12"
                }
                """;

    mockMvc
        .perform(
            post(loginBaseUrl).contentType(MediaType.APPLICATION_JSON).content(validJsonRequest))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.token").doesNotExist());
  }

  @Test
  public void login_shouldReturn400_whenInvalidEmail() throws Exception {
    String validJsonRequest =
        """
                {
                    "email": "john#example.com",
                    "password": "password123"
                }
                """;

    mockMvc
        .perform(
            post(loginBaseUrl).contentType(MediaType.APPLICATION_JSON).content(validJsonRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(jsonPath("$.message").exists());
  }

  private AuthResponse generateRefreshToken() throws Exception {
    var user = new User();
    user.setEmail("john@example.com");
    user.setName("John Doe");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user);

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

    return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AuthResponse.class);
  }

  @Test
  public void refresh_shouldReturn200_whenValidRefreshToken() throws Exception {
    var refreshToken = generateRefreshToken().getRefreshToken();

    String refreshRequest =
        String.format(
            """
        {
          "refreshToken": "%s"
        }
        """,
            refreshToken.toString());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }

  @Test
  public void refresh_shouldReturn401_whenInvalidRefreshToken() throws Exception {
    generateRefreshToken();

    String refreshRequest =
        String.format(
            """
        {
          "refreshToken": "%s"
        }
        """,
            UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void refresh_shouldReturn401_whenRefreshTokenRevoked() throws Exception {
    var refreshToken = generateRefreshToken().getRefreshToken();

    String jsonRequest =
        String.format(
            """
        {
          "refreshToken": "%s"
        }
        """,
            refreshToken);

    mockMvc.perform(
        post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON).content(jsonRequest));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void refresh_shouldReturn401_whenRefreshTokenExpired() throws Exception {
    var token = generateRefreshToken().getRefreshToken();
    RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow();
    // Expire the token
    refreshToken.setValidTo(OffsetDateTime.now().plusDays(-10));

    String jsonRequest =
        String.format(
            """
        {
          "refreshToken": "%s"
        }
        """,
            token);
    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void refresh_shouldReturn400_whenRefreshTokenMissing() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void refresh_shouldReturn400_whenRefreshTokenEmpty() throws Exception {
    String jsonRequest =
        """
        {
          "refreshToken": null
        }
        """;
    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void logout_shouldReturn204_whenValidRefreshToken() throws Exception {
    var token = generateRefreshToken().getRefreshToken();

    String jsonRequest =
        String.format(
            """
        {
          "refreshToken": "%s"
        }
        """,
            token);

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isNoContent());
  }

  @Test
  public void logout_shouldReturn204_whenValidButRevokedRefreshToken() throws Exception {
    var token = generateRefreshToken().getRefreshToken();

    String jsonRequest =
        String.format(
            """
        {
          "refreshToken": "%s"
        }
        """,
            token);

    // Double logout - invalidates the token after 1st call
    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isNoContent());
  }

  @Test
  public void logout_shouldReturn204_whenInvalidRefreshToken() throws Exception {
    String jsonRequest =
        String.format(
            """
        {
          "refreshToken": "%s"
        }
        """,
            UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isNoContent());
  }

  @Test
  public void logout_shouldReturn400_whenEmptyRefreshToken() throws Exception {
    String jsonRequest =
        """
        {
          "refreshToken": null
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isBadRequest());
  }
}
