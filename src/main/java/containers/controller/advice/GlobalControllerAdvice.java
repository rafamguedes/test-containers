package containers.controller.advice;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalControllerAdvice {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
      EntityNotFoundException e) {
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("message", e.getMessage());
    errorDetails.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException e) {
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("message", e.getMessage());
    errorDetails.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleResponseStatusException(
      ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(Map.of("message", Objects.requireNonNull(ex.getReason())));
  }
}
