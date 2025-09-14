package containers.service;

import containers.dto.request.UserRequestDto;
import containers.dto.response.UserResponseDto;
import containers.dto.request.UserUpdateRequestDto;
import containers.security.RoleEnum;
import containers.mapper.UserMapper;
import containers.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
  private static final String USER_NOT_FOUND = "User not found for ID: ";
  private static final String USER_EMAIL_NOT_FOUND = "User not found for email: ";

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserResponseDto save(UserRequestDto requestDto) {
    var user = userMapper.toEntity(requestDto);
    user.setPassword(getEncodePassword(requestDto));
    user.setRole(RoleEnum.USER);
    var savedUser = userRepository.save(user);
    return userMapper.toResponseDto(savedUser);
  }

  public UserResponseDto findById(Long id) {
    return userRepository
        .findById(id)
        .map(userMapper::toResponseDto)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + id));
  }

  public void update(Long id, UserUpdateRequestDto updatedUser) {
    var existingUser =
        userRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + id));

    existingUser.setName(updatedUser.getName());
    existingUser.setEmail(updatedUser.getEmail());

    userRepository.save(existingUser);
  }

  public void delete(Long id) {
    var existingUser =
        userRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + id));

    userRepository.delete(existingUser);
  }

  private static String getEncodePassword(UserRequestDto requestDTO) {
    return new BCryptPasswordEncoder().encode(requestDTO.getPassword());
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    try {
      return userRepository
          .findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException(USER_EMAIL_NOT_FOUND + email));
    } catch (UsernameNotFoundException e) {
      log.error("Authentication error: {}", e.getMessage());
      throw e;
    }
  }
}
