package com.portalfacturacion.adapter.in.web.handler;

import com.portalfacturacion.domain.exception.ClienteDuplicadoException;
import com.portalfacturacion.domain.exception.ClienteNoEncontradoException;
import com.portalfacturacion.domain.exception.CredencialesInvalidasException;
import com.portalfacturacion.domain.exception.FacturaDuplicadaException;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.exception.FudoNoDisponibleException;
import com.portalfacturacion.domain.exception.TicketNoEncontradoException;
import com.portalfacturacion.domain.exception.UsuarioDuplicadoException;
import com.portalfacturacion.domain.exception.UsuarioInactivoException;
import com.portalfacturacion.domain.exception.UsuarioNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private ProblemDetail build(HttpStatus status, String title, String detail,
                              String typeSuffix, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setTitle(title);
    pd.setType(URI.create("https://api.portalfacturacion.com/problems/" + typeSuffix));
    pd.setInstance(URI.create(req.getRequestURI()));
    pd.setProperty("timestamp", Instant.now().toString());
    return pd;
  }

  @ExceptionHandler(UsuarioNoEncontradoException.class)
  public ProblemDetail notFound(UsuarioNoEncontradoException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "resource-not-found", req);
  }

  @ExceptionHandler(UsuarioDuplicadoException.class)
  public ProblemDetail conflict(UsuarioDuplicadoException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, "Resource conflict", ex.getMessage(), "resource-conflict", req);
  }

  @ExceptionHandler({CredencialesInvalidasException.class, UsuarioInactivoException.class})
  public ProblemDetail unauthorized(RuntimeException ex, HttpServletRequest req) {
    return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), "unauthorized", req);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String detail = ex.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, "Validation failed", detail, "validation-error", req);
  }

  @ExceptionHandler(ClienteNoEncontradoException.class)
  public ProblemDetail clienteNotFound(ClienteNoEncontradoException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "resource-not-found", req);
  }

  @ExceptionHandler(ClienteDuplicadoException.class)
  public ProblemDetail clienteConflict(ClienteDuplicadoException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, "Resource conflict", ex.getMessage(), "resource-conflict", req);
  }

  @ExceptionHandler(TicketNoEncontradoException.class)
  public ProblemDetail ticketNotFound(TicketNoEncontradoException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "resource-not-found", req);
  }

  @ExceptionHandler(FacturaNoEncontradaException.class)
  public ProblemDetail facturaNotFound(FacturaNoEncontradaException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "resource-not-found", req);
  }

  @ExceptionHandler(FacturaDuplicadaException.class)
  public ProblemDetail facturaConflict(FacturaDuplicadaException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, "Resource conflict", ex.getMessage(), "resource-conflict", req);
  }

  @ExceptionHandler(FudoNoDisponibleException.class)
  public ProblemDetail fudoUnavailable(FudoNoDisponibleException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_GATEWAY, "Upstream unavailable", ex.getMessage(), "fudo-unavailable", req);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail forbidden(AccessDeniedException ex, HttpServletRequest req) {
    return build(HttpStatus.FORBIDDEN, "Forbidden", "No tiene permisos suficientes", "forbidden", req);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail auth(AuthenticationException ex, HttpServletRequest req) {
    return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Token no valido o ausente", "unauthorized", req);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail constraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
    String detail = ex.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, "Validation failed", detail, "validation-error", req);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail missingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "Missing parameter", ex.getMessage(), "missing-parameter", req);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail badRequest(IllegalArgumentException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), "bad-request", req);
  }
}
