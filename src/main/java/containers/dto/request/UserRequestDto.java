package containers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {

  @NotBlank(message = "Name cannot be empty.")
  @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
  private String name;

  @NotBlank(message = "Email cannot be empty.")
  @Email(message = "Email must be valid.")
  private String email;

  @NotBlank(message = "Password cannot be empty.")
  @Size(min = 8, message = "Password must be at least 8 characters long.")
  private String password;
}
