package cc.suvankar.moneytrail.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.suvankar.moneytrail.auth.dto.AuthResponse;
import cc.suvankar.moneytrail.auth.exception.EmailAlreadyExistsException;
import cc.suvankar.moneytrail.auth.exception.InvalidCredentialsException;
import cc.suvankar.moneytrail.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthService authService;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  private static final String registerBaseUrl = "/api/v1/auth/register";
  private static final String loginBaseUrl = "/api/v1/auth/login";

  @Test
  void register_shouldReturn201_whenValidRequest() throws Exception {
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

    verify(authService, times(1)).registerUser(any());
  }

  @Test
  void register_shouldReturn400_whenEmailIsMalformed() throws Exception {
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
        .andExpect(
            result -> {
              var json = result.getResponse().getContentAsString();
              var errorResponse = objectMapper.readValue(json, ErrorResponse.class);
              assertThat(errorResponse.getMessage()).contains("Must be a valid email address");
            });
  }

  @Test
  void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
    String invalidJsonRequest =
        """
                {
                    "name": "John Doe",
                    "email": "john@exampe.com",
                    "password": "password123"
                }
                """;

    doThrow(new EmailAlreadyExistsException("The requested email is already in use."))
        .when(authService)
        .registerUser(any());

    mockMvc
        .perform(
            post(registerBaseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonRequest))
        .andExpect(status().isConflict())
        .andExpect(
            result -> {
              var json = result.getResponse().getContentAsString();
              var errorResponse = objectMapper.readValue(json, ErrorResponse.class);
              assertThat(errorResponse.getMessage())
                  .isEqualTo("The requested email is already in use.");
            });
  }

  @Test
  void login_shouldReturn200AndJwtToken_whenCredentialIsValid() throws Exception {
    var validJson =
        """
                {
                    "email": "john@exampe.com",
                    "password": "password123"
                }
                """;

    when(authService.loginUser(any())).thenReturn(new AuthResponse("dummy-jwt-token"));

    mockMvc
        .perform(post(loginBaseUrl).contentType(MediaType.APPLICATION_JSON).content(validJson))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              var json = result.getResponse().getContentAsString();
              var authResponse = objectMapper.readValue(json, AuthResponse.class);

              assertThat(authResponse.getToken()).isEqualTo("dummy-jwt-token");
            });
  }

  @Test
  void login_shouldReturn401_whenCredentialIsInvalid() throws Exception {
    var invalidCredJson =
        """
                {
                    "email": "john@exampe.com",
                    "password": "password123"
                }
                """;

    when(authService.loginUser(any()))
        .thenThrow(new InvalidCredentialsException("Invalid credentials."));

    mockMvc
        .perform(
            post(loginBaseUrl).contentType(MediaType.APPLICATION_JSON).content(invalidCredJson))
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> {
              var json = result.getResponse().getContentAsString();
              var errorResponse = objectMapper.readValue(json, ErrorResponse.class);
              assertThat(errorResponse.getMessage()).isEqualTo("Invalid credentials.");
            });
  }
}
