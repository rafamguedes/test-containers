package containers.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import containers.entity.User;
import containers.security.RoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

public class UserIntegrationTest extends BaseIntegrationTest {

  private static final String BASE_URL = "/users/%s";
  private static final String USER_NOT_FOUND_MESSAGE = "User not found for ID: ";

  @Test
  public void shouldCreateUserSuccessfully() throws Exception {
    var userJsonRequest =
        String.format(
            """
            {
              "name": "%s",
              "email": "%s",
              "password": "%s"
            }
            """,
            userFirstName, userEmail, userPasswordDecrypted);

    mockMvc
        .perform(
            post("/users")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJsonRequest))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.name").value(userFirstName))
        .andExpect(jsonPath("$.email").value(userEmail));
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
