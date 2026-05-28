package cc.suvankar.moneytrail.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.suvankar.moneytrail.auth.JwtUtil;
import cc.suvankar.moneytrail.tag.dto.TagResponse;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class TagControllerIT {
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired JwtUtil jwtUtil;
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  private String user1Token;
  private String user2Token;

  @BeforeEach
  public void setup() throws Exception {
    User user1 = new User();
    user1.setName("John Doe");
    user1.setEmail("john@example.com");
    user1.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user1);

    user1Token = jwtUtil.generateTokenWithUserId(user1.getEmail(), user1.getId());

    User user2 = new User();
    user2.setName("John Doe");
    user2.setEmail("john2@example.com");
    user2.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user2);

    user2Token = jwtUtil.generateTokenWithUserId(user2.getEmail(), user2.getId());

    String json =
        """
        {
            "tagName": "Food"
        }
        """;

    mockMvc.perform(
        post("/api/v1/tags")
            .header("Authorization", "Bearer " + user1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json));

    json =
        """
        {
            "tagName": "Travel"
        }
        """;
    mockMvc.perform(
        post("/api/v1/tags")
            .header("Authorization", "Bearer " + user1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json));
  }

  @Test
  public void createTag_shouldReturn201AndTag_whenValidRequest() throws Exception {
    String json =
        """
        {
            "tagName": "Shopping"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/tags")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tagId").isNotEmpty())
        .andExpect(jsonPath("$.tagName").value("Shopping"));
  }

  @Test
  public void getTag_shouldReturn200AndTag_whenTagIsValid() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/tags/Food")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    TagResponse response =
        objectMapper.readValue(result.getResponse().getContentAsString(), TagResponse.class);

    assertThat(response).isNotNull();
    assertThat(response.tagId()).isNotNull();
    assertThat(response.tagName()).isEqualTo("Food");
  }

  @Test
  public void getTags_shouldReturn200AndTags_whenUserIsValid() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/tags")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    List<TagResponse> tags =
        objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(tags).hasSize(2);
  }

  @Test
  public void deleteTag_shouldReturn204_whenValidTag() throws Exception {
    mockMvc
        .perform(delete("/api/v1/tags/Food").header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/v1/tags/Food")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getTag_shouldReturn404_whenWrongUserToken() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/tags/Food")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteTag_shouldReturn404_whenTagDoesNotBelongToUser() throws Exception {
    mockMvc
        .perform(delete("/api/v1/tags/Food").header("Authorization", "Bearer " + user2Token))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteTag_shouldReturn404_whenTagDoesNotExist() throws Exception {
    mockMvc
        .perform(delete("/api/v1/tags/Foods").header("Authorization", "Bearer " + user1Token))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createTag_shouldReturn401_whenInvalidJwt() throws Exception {
    String json =
        """
        {
            "tagName": "Shopping"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/tags")
                .header("Authorization", "Bearer " + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void getTag_shouldReturn404_whenTagDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/tags/Foods")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
