package containers.security;

import containers.entity.User;
import containers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitializerConfig {

  private final UserRepository userRepository;

  @Value("${admin.email}")
  private String adminEmail;

  @Value("${admin.password}")
  private String adminPassword;

  @Value("${admin.first-name}")
  private String adminFirstName;

  @EventListener(ApplicationReadyEvent.class)
  public void initAdminUser() {
    if (userRepository.findByEmail(adminEmail).isEmpty()) {
      User admin =
          User.builder()
              .name(adminFirstName)
              .email(adminEmail)
              .password(adminPassword)
              .role(RoleEnum.ADMIN)
              .build();

      userRepository.save(admin);
      log.info("Admin user created successfully: {}", adminEmail);
    } else {
      log.info("Admin user already exists: {}", adminEmail);
    }
  }
}
