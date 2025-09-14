package containers.mapper;

import containers.dto.request.UserRequestDto;
import containers.dto.response.UserResponseDto;
import containers.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  User toEntity(UserRequestDto userRequestDto);

  @Mapping(source = "id", target = "id")
  UserResponseDto toResponseDto(User user);
}
