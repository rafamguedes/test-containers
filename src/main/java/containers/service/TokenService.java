package containers.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TokenService {

  private final Algorithm algorithm;
  private final UserService userService;

  @Value("${spring.security.token.secret}")
  private String secret;

  @Value("${spring.security.token.expiration}")
  private Long accessTokenExpiration;

  @Value("${spring.security.refresh.token.expiration}")
  private Long refreshTokenExpiration;

  // Set to store invalidated tokens
  private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

  public TokenService(
      @Value("${spring.security.token.secret}") String secret, UserService userService) {
    this.algorithm = Algorithm.HMAC256(secret);
    this.userService = userService;
  }

  public String generateToken(String email) {
    var userDetails = userService.loadUserByUsername(email);
    var roles =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

    return JWT.create()
        .withSubject(email)
        .withClaim("roles", roles)
        .withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
        .sign(algorithm);
  }

  private Instant generateExpiration() {
    return Instant.now().plus(accessTokenExpiration, ChronoUnit.MINUTES);
  }

  public String validateToken(String token) {
    if (isTokenInvalid(token)) {
      throw new IllegalArgumentException("Token inválido ou revogado");
    }
    try {
      return JWT.require(algorithm).build().verify(token).getSubject();
    } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
      throw new IllegalArgumentException("Token expirado", e);
    } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
      throw new IllegalArgumentException("Token inválido", e);
    }
  }

  public String generateRefreshToken(String email) {
    return JWT.create()
        .withSubject(email)
        .withExpiresAt(Instant.now().plus(refreshTokenExpiration, ChronoUnit.HOURS))
        .sign(algorithm);
  }

  public void invalidateToken(String token) {
    invalidatedTokens.add(token);
  }

  public boolean isTokenInvalid(String token) {
    return invalidatedTokens.contains(token);
  }
}
