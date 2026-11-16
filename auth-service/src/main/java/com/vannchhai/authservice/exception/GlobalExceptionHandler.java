package com.vannchhai.authservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ProblemDetail handleUserExists(UserAlreadyExistsException ex) {
    return problem(HttpStatus.CONFLICT, "User already exists", ex.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
    return problem(
        HttpStatus.UNAUTHORIZED, "Authentication failed", "Invalid username or password");
  }

  @ExceptionHandler({AccountLockedException.class, LockedException.class})
  public ProblemDetail handleAccountLocked(RuntimeException ex) {
    return problem(HttpStatus.LOCKED, "Account locked", ex.getMessage());
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
    return problem(HttpStatus.UNAUTHORIZED, "Invalid refresh token", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    log.error("Unhandled exception", ex);
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal error",
        "Something went wrong. Please try again.");
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    Map<String, String> fieldErrors = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

    ProblemDetail detail =
        problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid");
    detail.setProperty("errors", fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
  }

  private ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setTitle(title);
    pd.setType(URI.create("about:blank"));
    return pd;
  }
}
