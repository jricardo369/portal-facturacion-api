package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.request.ActualizarConfiguracionRequest;
import com.portalfacturacion.adapter.in.web.response.ConfiguracionItemResponse;
import com.portalfacturacion.application.port.in.contacto.ActualizarConfiguracionUseCase;
import com.portalfacturacion.application.port.in.contacto.ObtenerConfiguracionesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/configuraciones")
@Tag(name = "Configuraciones", description = "Consulta y edicion de las configuraciones generales del portal (tabla configuracion)")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracionController {

  private final ObtenerConfiguracionesUseCase obtenerUseCase;
  private final ActualizarConfiguracionUseCase actualizarUseCase;

  public ConfiguracionController(ObtenerConfiguracionesUseCase obtenerUseCase,
      ActualizarConfiguracionUseCase actualizarUseCase) {
    this.obtenerUseCase = obtenerUseCase;
    this.actualizarUseCase = actualizarUseCase;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','CONSULTA')")
  @Operation(summary = "Obtener todas las configuraciones",
      description = "Devuelve todas las filas de la tabla configuracion (clave, valor, descripcion, estado).")
  public ResponseEntity<List<ConfiguracionItemResponse>> obtenerTodas() {
    return ResponseEntity.ok(obtenerUseCase.obtenerTodas().stream()
        .map(ConfiguracionItemResponse::from).toList());
  }

  @PutMapping("/{clave}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Editar una configuracion por clave",
      description = "Actualiza el valor (y opcionalmente descripcion y estado) de una configuracion existente. "
          + "Ejemplo: clave correo_dudas_aclaraciones para el correo de dudas y aclaraciones.")
  public ResponseEntity<ConfiguracionItemResponse> actualizar(
      @PathVariable String clave,
      @Valid @RequestBody ActualizarConfiguracionRequest request) {
    return ResponseEntity.ok(ConfiguracionItemResponse.from(
        actualizarUseCase.actualizar(clave, request.valor(), request.descripcion(), request.activo())));
  }
}
