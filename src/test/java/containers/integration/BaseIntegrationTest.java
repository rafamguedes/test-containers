package containers.integration;

import containers.entity.User;
import containers.repository.UserRepository;
import containers.security.RoleEnum;
import containers.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {
  @Autowired protected UserRepository userRepository;

  @Autowired protected MockMvc mockMvc;

  @Autowired protected TokenService tokenService;

  @Autowired protected BCryptPasswordEncoder passwordEncoder;

  protected String jwtToken;

  @BeforeEach
  protected void setUp() {
    userRepository.deleteAll();

    var adminUser =
        User.builder()
            .name("Admin")
            .email("admin@email.com")
            .password(passwordEncoder.encode("12345678"))
            .role(RoleEnum.ADMIN)
            .build();

    userRepository.save(adminUser);

    jwtToken = "Bearer " + tokenService.generateToken(adminUser.getEmail());
  }
}
