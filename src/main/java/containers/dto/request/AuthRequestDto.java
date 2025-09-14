package containers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequestDto {
  @NotBlank(message = "Email cannot be empty.")
  @Email(message = "Email must be valid.")
  private String email;

  @NotBlank(message = "Password cannot be empty.")
  @Size(min = 8, message = "Password must be at least 8 characters long.")
  private String password;
}
