package containers.security;

import containers.service.TokenService;
import containers.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final UserService userService;

  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer ";

  @Autowired
  public JwtFilter(TokenService tokenService, UserService userService) {
    this.tokenService = tokenService;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      var token = extractToken(request);
      if (token.isPresent()) {
        log.debug("Token found: {}", token.get());

        // Extract email and roles from token
        var email = tokenService.validateToken(token.get());
        log.debug("Token subject: {}", email);

        // Get user details to get authorities
        var userDetails = userService.loadUserByUsername(email);
        log.debug("User authorities: {}", userDetails.getAuthorities());

        var authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authentication set in security context");
      } else {
        log.debug("No token found in request");
      }
    } catch (IllegalArgumentException e) {
      log.error("Error validating token: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Invalid or expired token");
      return;
    } catch (Exception e) {
      log.error("Unexpected error: {}", e.getMessage(), e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("Internal server error");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractToken(HttpServletRequest request) {
    var authHeader = request.getHeader(AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith(BEARER)) {
      return Optional.empty();
    }
    return Optional.of(authHeader.substring(BEARER.length()));
  }
}
