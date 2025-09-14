package containers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDto {

  @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
  private String name;

  @Email(message = "Email must be valid.")
  private String email;
}
