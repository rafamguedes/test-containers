package containers.controller;

import containers.dto.request.AuthRequestDto;
import containers.dto.response.AuthResponseTokenDto;
import containers.service.exception.BadRequestException;
import containers.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password.";
  private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponseTokenDto> login(@Valid @RequestBody AuthRequestDto req) {
    try {
      var auth =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

      var accessToken = tokenService.generateToken(auth.getName());
      var refreshToken = tokenService.generateRefreshToken(auth.getName());

      return ResponseEntity.ok(new AuthResponseTokenDto(accessToken, refreshToken));
    } catch (BadCredentialsException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_EMAIL_OR_PASSWORD);
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponseTokenDto> refresh(@RequestBody String refreshToken) {
    try {
      var email = tokenService.validateToken(refreshToken);
      var newAccessToken = tokenService.generateToken(email);

      return ResponseEntity.ok(new AuthResponseTokenDto(newAccessToken, refreshToken));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_REFRESH_TOKEN);
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
    tokenService.invalidateToken(token.replace("Bearer ", ""));
    return ResponseEntity.noContent().build();
  }
}
