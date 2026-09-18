package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.request.LoginRequest;
import com.portalfacturacion.adapter.in.web.response.LoginResponse;
import com.portalfacturacion.application.service.AuthResult;
import com.portalfacturacion.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Iniciar sesion y obtener JWT")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResult result = authService.login(request.usuario(), request.password());
    String tipo = result.usuario().getTipo() == null ? null : result.usuario().getTipo().name();
    return ResponseEntity.ok(LoginResponse.bearer(result.token(), result.usuario().getId(),
        result.usuario().getUsuario(), tipo));
  }
}
