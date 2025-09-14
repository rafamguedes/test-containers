package containers.integration;

import containers.entity.User;
import containers.repository.UserRepository;
import containers.security.RoleEnum;
import containers.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${admin.first-name}")
  protected String adminFirstName;

  @Value("${admin.email}")
  protected String adminEmail;

  @Value("${admin.password}")
  protected String adminPassword;

  @Value("${admin.password-decrypted}")
  protected String adminPasswordDecrypted;

  @Value("${user.first-name}")
  protected String userFirstName;

  @Value("${user.email}")
  protected String userEmail;

  @Value("${user.password}")
  protected String userPassword;

  @Value("${user.password-decrypted}")
  protected String userPasswordDecrypted;

  @Autowired protected UserRepository userRepository;

  @Autowired protected MockMvc mockMvc;

  @Autowired protected TokenService tokenService;

  protected String jwtToken;

  @BeforeEach
  protected void setUp() {
    userRepository.deleteAll();

    var adminUser =
        User.builder()
            .name(adminFirstName)
            .email(adminEmail)
            .password(adminPassword)
            .role(RoleEnum.ADMIN)
            .build();

    userRepository.save(adminUser);

    jwtToken = "Bearer " + tokenService.generateToken(adminUser.getEmail());
  }

  protected User userMock() {
    return User.builder()
        .name(userFirstName)
        .email(userEmail)
        .password(userPassword)
        .role(RoleEnum.USER)
        .build();
  }
}
