package containers.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import containers.entity.User;
import containers.security.RoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class UserIntegrationTest extends BaseIntegrationTest {

  private static final String BASE_URL = "/users/%s";
  private static final String USER_NOT_FOUND_MESSAGE = "User not found for ID: ";

  private User userMock() {
    return User.builder()
        .name("João")
        .email("joao@email.com")
        .password(passwordEncoder.encode("12345678"))
        .role(RoleEnum.USER)
        .build();
  }

  @Test
  public void shouldCreateUserSuccessfully() throws Exception {
    var userJsonRequest =
        """
            {
              "name": "João",
              "email": "joao@email.com",
              "password": "12345678"
            }
            """;

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJsonRequest))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.name").value("João"))
        .andExpect(jsonPath("$.email").value("joao@email.com"));
  }

  @Test
  public void shouldFindUserById() throws Exception {
    var user = userRepository.save(userMock());

    var findByIdUrl = BASE_URL.formatted(user.getId());

    mockMvc
        .perform(
            get(findByIdUrl).header("Authorization", jwtToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId()))
        .andExpect(jsonPath("$.name").value(user.getName()))
        .andExpect(jsonPath("$.email").value(user.getEmail()));
  }

  @Test
  public void shouldUpdateUserSuccessfully() throws Exception {
    var user = userRepository.save(userMock());

    var updatedUserJsonRequest =
        """
            {
              "name": "Maria",
              "email": "maria@email.com"
            }
            """;

    var updateUrl = BASE_URL.formatted(user.getId());

    mockMvc
        .perform(
            put(updateUrl)
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserJsonRequest))
        .andExpect(status().isNoContent());
  }

  @Test
  public void shouldDeleteUserSuccessfully() throws Exception {
    var user = userRepository.save(userMock());

    var deleteUrl = BASE_URL.formatted(user.getId());

    mockMvc
        .perform(delete(deleteUrl).header("Authorization", jwtToken))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get(deleteUrl).header("Authorization", jwtToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(USER_NOT_FOUND_MESSAGE + user.getId()));
  }
}
