package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.request.ActualizarUsuarioRequest;
import com.portalfacturacion.adapter.in.web.request.CrearUsuarioRequest;
import com.portalfacturacion.adapter.in.web.response.PaginaUsuarioResponse;
import com.portalfacturacion.adapter.in.web.response.UsuarioResponse;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.service.UsuarioService;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

  private final UsuarioService service;

  public UsuarioController(UsuarioService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','CONSULTA')")
  @Operation(summary = "Listar usuarios activos con paginacion y filtros")
  public ResponseEntity<PaginaUsuarioResponse> listar(
      @RequestParam(required = false) String usuario,
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) TipoUsuario tipo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    PagedResult<Usuario> result = service.listar(usuario, nombre, tipo, page, size, sort);
    return ResponseEntity.ok(PaginaUsuarioResponse.from(result, this::toResponse));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','CONSULTA')")
  @Operation(summary = "Obtener usuario por id")
  public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
    return ResponseEntity.ok(toResponse(service.obtenerPorId(id)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Crear usuario")
  public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
    Usuario creado = service.crear(new Usuario(null, request.usuario(), request.password(),
        request.nombre(), request.direccion(), request.telefono(), request.tipo(), true, null, null));
    return ResponseEntity.created(URI.create("/api/v1/usuarios/" + creado.getId()))
        .body(toResponse(creado));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Actualizar usuario")
  public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody ActualizarUsuarioRequest request) {
    Usuario actualizado = service.actualizar(id, new Usuario(id, request.usuario(), request.password(),
        request.nombre(), request.direccion(), request.telefono(), request.tipo(), true, null, null));
    return ResponseEntity.ok(toResponse(actualizado));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Baja logica de usuario")
  public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    service.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  private UsuarioResponse toResponse(Usuario u) {
    return new UsuarioResponse(u.getId(), u.getUsuario(), u.getNombre(), u.getDireccion(),
        u.getTelefono(), u.getTipo(), u.getActivo(), u.getCreatedAt(), u.getUpdatedAt());
  }
}
