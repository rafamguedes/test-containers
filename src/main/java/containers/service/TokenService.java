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
  private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

  @Value("${spring.security.token.expiration}")
  private Long accessTokenExpiration;

  @Value("${spring.security.refresh.token.expiration}")
  private Long refreshTokenExpiration;

  public TokenService(@Value("${spring.security.token.secret}") String secret) {
    this.algorithm = Algorithm.HMAC256(secret);
  }

  public String generateToken(String email) {
    return JWT.create()
        .withSubject(email)
        .withExpiresAt(calculateExpiration(accessTokenExpiration * 60 * 1000)) // minutes to ms
        .sign(algorithm);
  }

  public String generateRefreshToken(String email) {
    return JWT.create()
        .withSubject(email)
        .withExpiresAt(calculateExpiration(refreshTokenExpiration * 60 * 60 * 1000)) // hours to ms
        .sign(algorithm);
  }

  private Date calculateExpiration(long expirationMs) {
    return new Date(System.currentTimeMillis() + expirationMs);
  }

  public String validateToken(String token) {
    if (isTokenInvalid(token)) {
      throw new IllegalArgumentException("Invalid token - token has been revoked");
    }
    try {
      return JWT.require(algorithm).build().verify(token).getSubject();
    } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
      throw new IllegalArgumentException("Expiration token ", e);
    } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
      throw new IllegalArgumentException("Invalid token ", e);
    }
  }

  public void invalidateToken(String token) {
    invalidatedTokens.add(token);
  }

  public boolean isTokenInvalid(String token) {
    return invalidatedTokens.contains(token);
  }
}