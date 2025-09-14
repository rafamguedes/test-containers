package containers.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

public class AuthIntegrationTest extends BaseIntegrationTest {

  @Test
  public void shouldAuthenticateWithValidCredentials() throws Exception {
    var loginRequest =
        String.format(
            """
            {
              "email": "%s",
              "password": "%s"
            }
            """,
            adminEmail, adminPasswordDecrypted);

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty());
  }

  @Test
  public void shouldFailAuthenticationWithInvalidCredentials() throws Exception {
    var loginRequest =
        """
            {
              "email": "error@email.com",
              "password": "wrongPassword"
            }
            """;

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid email or password."));
  }
}
