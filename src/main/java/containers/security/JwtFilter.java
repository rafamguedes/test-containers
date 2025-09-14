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
      Optional<String> token = extractToken(request);

      if (token.isEmpty()) {
        log.debug("No token found in request");
        filterChain.doFilter(request, response);
        return;
      }

      String tokenValue = token.get();
      log.debug("Token found: {}", tokenValue);

      // 1. Verifica se token foi invalidado
      if (tokenService.isTokenInvalid(tokenValue)) {
        log.warn("Attempt to use revoked token");
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token revogado");
        return;
      }

      // 2. Valida token e extrai email
      String email = tokenService.validateToken(tokenValue);
      log.debug("Token subject: {}", email);

      // 3. Carrega UserDetails (ou usa authorities do token)
      var userDetails = userService.loadUserByUsername(email);
      log.debug("User authorities: {}", userDetails.getAuthorities());

      // 4. Cria autenticação
      var authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.debug("Authentication set in security context");

    } catch (IllegalArgumentException e) {
      handleJwtException(e, response);
      return;
    } catch (Exception e) {
      log.error("Unexpected error during JWT filtering: {}", e.getMessage(), e);
      sendErrorResponse(
          response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader(AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith(BEARER)) {
      return Optional.empty();
    }
    return Optional.of(authHeader.substring(BEARER.length()));
  }

  private void handleJwtException(IllegalArgumentException e, HttpServletResponse response)
      throws IOException {
    String message = e.getMessage();
    log.error("JWT validation error: {}", message);

    if (message.contains("expirado")) {
      sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");
    } else if (message.contains("revogado") || message.contains("inválido")) {
      sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
    } else {
      sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Erro de autenticação");
    }
  }

  private void sendErrorResponse(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\": \"" + message + "\"}");
  }

  // Opcional: Pular filtro para endpoints públicos
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/auth/login") || path.startsWith("/auth/refresh");
  }
}
