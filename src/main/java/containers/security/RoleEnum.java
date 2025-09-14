package containers.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
  ADMIN("ROLE_ADMIN"), // Add ROLE_ prefix
  USER("ROLE_USER"); // Add ROLE_ prefix

  private final String name;
}
