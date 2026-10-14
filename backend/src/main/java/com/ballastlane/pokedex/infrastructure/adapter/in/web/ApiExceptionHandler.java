package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import com.ballastlane.pokedex.domain.exception.ConcurrentModificationException;
import com.ballastlane.pokedex.domain.exception.DomainException;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.InvalidPaginationException;
import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException;
import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException.FieldViolation;
import com.ballastlane.pokedex.domain.exception.InvalidUserDataException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.exception.UserNotFoundException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Maps every error to RFC 7807 {@link ProblemDetail}. Internal details never reach the client; the
 * full stack trace is logged together with the correlation id.
 */
@RestControllerAdvice
class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(InvalidPaginationException.class)
  ProblemDetail invalidPagination(InvalidPaginationException ex) {
    return problem(HttpStatus.BAD_REQUEST, "Invalid pagination", ex.getMessage());
  }

  @ExceptionHandler(InvalidPokemonDataException.class)
  ProblemDetail invalidData(InvalidPokemonDataException ex) {
    ProblemDetail problem =
        problem(HttpStatus.BAD_REQUEST, "Invalid Pokémon data", ex.getMessage());
    problem.setProperty("errors", ex.getViolations());
    return problem;
  }

  @ExceptionHandler(InvalidUserDataException.class)
  ProblemDetail invalidUser(InvalidUserDataException ex) {
    ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid user data", ex.getMessage());
    problem.setProperty("errors", ex.getViolations());
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail validation(MethodArgumentNotValidException ex) {
    List<FieldViolation> violations =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new FieldViolation(e.getField(), e.getDefaultMessage()))
            .toList();
    ProblemDetail problem =
        problem(HttpStatus.BAD_REQUEST, "Invalid request", "One or more fields are invalid");
    problem.setProperty("errors", violations);
    return problem;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ProblemDetail unreadable(HttpMessageNotReadableException ex) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "Malformed request",
        "Request body is missing, malformed or contains unknown fields");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ProblemDetail typeMismatch(MethodArgumentTypeMismatchException ex) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "Invalid parameter",
        "parameter '" + ex.getName() + "' has an invalid value");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ProblemDetail missingParameter(MissingServletRequestParameterException ex) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "Missing parameter",
        "parameter '" + ex.getParameterName() + "' is required");
  }

  @ExceptionHandler({PokemonNotFoundException.class, UserNotFoundException.class})
  ProblemDetail notFound(RuntimeException ex) {
    return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ProblemDetail noResource() {
    return problem(HttpStatus.NOT_FOUND, "Not found", "No endpoint matches this request");
  }

  @ExceptionHandler({DuplicateResourceException.class, ConcurrentModificationException.class})
  ProblemDetail conflict(RuntimeException ex) {
    return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ProblemDetail unauthorized(InvalidCredentialsException ex) {
    return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ProblemDetail methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
    return problem(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Method not allowed",
        "method " + ex.getMethod() + " is not supported for this endpoint");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  ProblemDetail unsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
    return problem(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Unsupported media type",
        "use Content-Type: application/json");
  }

  @ExceptionHandler(ExternalServiceException.class)
  ProblemDetail externalService(ExternalServiceException ex) {
    log.warn("PokeAPI call failed: {}", ex.getMessage(), ex);
    return problem(
        HttpStatus.BAD_GATEWAY,
        "Upstream service unavailable",
        "PokeAPI is unavailable or timed out. Try again later.");
  }

  /** Lets Spring Security turn method-level denials into 403 instead of a 500. */
  @ExceptionHandler(AccessDeniedException.class)
  void accessDenied(AccessDeniedException ex) throws AccessDeniedException {
    throw ex;
  }

  @ExceptionHandler(DomainException.class)
  ProblemDetail domainFallback(DomainException ex) {
    log.error("Unmapped domain error", ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Unexpected error");
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail unexpected(Exception ex) {
    log.error("Unhandled error", ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Unexpected error");
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    problem.setType(URI.create("about:blank"));
    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
    if (correlationId != null) {
      problem.setProperty("correlationId", correlationId);
    }
    return problem;
  }
}
