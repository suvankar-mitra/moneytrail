package cc.suvankar.moneytrail.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@Transactional
public class AuthControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

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
}
