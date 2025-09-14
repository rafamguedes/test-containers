package containers.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class AuthIntegrationTest extends BaseIntegrationTest {

  @Test
  public void shouldAuthenticateWithValidCredentials() throws Exception {
    var loginRequest =
        """
            {
              "email": "admin@email.com",
              "password": "12345678"
            }
            """;

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty());
  }
}
