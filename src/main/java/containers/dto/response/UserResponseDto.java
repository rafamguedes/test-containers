package containers.dto.response;

import lombok.Data;

@Data
public class UserResponseDto {
  private Long id;
  private String name;
  private String email;
}
