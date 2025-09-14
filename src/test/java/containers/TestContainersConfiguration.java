package containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("resource")
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("test_container_db")
        .withUsername("root")
        .withPassword("root")
        .withReuse(true);
  }
}
