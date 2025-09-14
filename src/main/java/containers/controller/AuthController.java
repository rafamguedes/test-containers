package containers.controller;

import containers.dto.request.AuthRequestDto;
import containers.dto.response.AuthResponseTokenDto;
import containers.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password.";
  private static final String INVALID_REFRESH_TOKEN = "Refresh token inválido.";

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponseTokenDto> login(@Valid @RequestBody AuthRequestDto req)
      throws BadRequestException {
    try {
      var pass = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
      var auth = authenticationManager.authenticate(pass);

      var accessToken = tokenService.generateToken(auth.getName());
      var refreshToken = tokenService.generateRefreshToken(auth.getName());

      return ResponseEntity.ok(new AuthResponseTokenDto(accessToken, refreshToken));
    } catch (BadCredentialsException e) {
      throw new BadRequestException(INVALID_USERNAME_OR_PASSWORD);
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponseTokenDto> refresh(@RequestBody String refreshToken)
      throws BadRequestException {
    try {
      var email = tokenService.validateToken(refreshToken);
      var newAccessToken = tokenService.generateToken(email);

      return ResponseEntity.ok(new AuthResponseTokenDto(newAccessToken, refreshToken));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(INVALID_REFRESH_TOKEN);
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
    tokenService.invalidateToken(token.replace("Bearer ", ""));
    return ResponseEntity.noContent().build();
  }
}
