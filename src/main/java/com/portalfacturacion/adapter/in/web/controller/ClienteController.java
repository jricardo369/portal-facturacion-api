package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.request.ActualizarClienteRequest;
import com.portalfacturacion.adapter.in.web.request.CrearClienteRequest;
import com.portalfacturacion.adapter.in.web.response.ClienteResponse;
import com.portalfacturacion.adapter.in.web.response.PaginaClienteResponse;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.service.ClienteService;
import com.portalfacturacion.domain.model.Cliente;
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
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

  private final ClienteService service;

  public ClienteController(ClienteService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','CONSULTA')")
  @Operation(summary = "Listar clientes activos con paginacion y filtros")
  public ResponseEntity<PaginaClienteResponse> listar(
      @RequestParam(required = false) String rfc,
      @RequestParam(required = false) String razonSocial,
      @RequestParam(required = false) String codigoPostal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "idCliente,desc") String sort) {
    PagedResult<Cliente> result = service.listar(rfc, razonSocial, codigoPostal, page, size, sort);
    return ResponseEntity.ok(PaginaClienteResponse.from(result, this::toResponse));
  }

  @GetMapping("/rfc/{rfc}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','CONSULTA')")
  @Operation(summary = "Obtener cliente por RFC")
  public ResponseEntity<ClienteResponse> obtenerPorRfc(@PathVariable String rfc) {
    return ResponseEntity.ok(toResponse(service.obtenerPorRfc(rfc)));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
  @Operation(summary = "Crear cliente")
  public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody CrearClienteRequest request) {
    Cliente creado = service.crear(toDomain(null, request.rfc(), request.razonSocial(),
        request.calle(), request.numExterior(), request.numInterior(), request.referencia(),
        request.estado(), request.municipio(), request.colonia(), request.codigoPostal(),
        request.correoElectronico(), request.regimenFiscal(), request.usoFactura()));
    return ResponseEntity.created(URI.create("/api/v1/clientes/rfc/" + creado.getRfc()))
        .body(toResponse(creado));
  }

  @PutMapping("/rfc/{rfc}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
  @Operation(summary = "Actualizar cliente por RFC")
  public ResponseEntity<ClienteResponse> actualizarPorRfc(@PathVariable String rfc,
                                                         @Valid @RequestBody ActualizarClienteRequest request) {
    Cliente cambios = toDomain(null, rfc, request.razonSocial(),
        request.calle(), request.numExterior(), request.numInterior(), request.referencia(),
        request.estado(), request.municipio(), request.colonia(), request.codigoPostal(),
        request.correoElectronico(), request.regimenFiscal(), request.usoFactura());
    return ResponseEntity.ok(toResponse(service.actualizarPorRfc(rfc, cambios)));
  }

  @DeleteMapping("/rfc/{rfc}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Baja logica de cliente por RFC")
  public ResponseEntity<Void> eliminarPorRfc(@PathVariable String rfc) {
    service.eliminarPorRfc(rfc);
    return ResponseEntity.noContent().build();
  }

  private Cliente toDomain(Long idCliente, String rfc, String razonSocial, String calle,
                           String numExterior, String numInterior, String referencia,
                           String estado, String municipio, String colonia, String codigoPostal,
                           String correoElectronico, String regimenFiscal, String usoFactura) {
    return new Cliente(idCliente, rfc, razonSocial, calle, numExterior, numInterior,
        referencia, estado, municipio, colonia, codigoPostal, correoElectronico,
        regimenFiscal, usoFactura, true, null, null);
  }

  private ClienteResponse toResponse(Cliente c) {
    return new ClienteResponse(c.getIdCliente(), c.getRfc(), c.getRazonSocial(), c.getCalle(),
        c.getNumExterior(), c.getNumInterior(), c.getReferencia(), c.getEstado(),
        c.getMunicipio(), c.getColonia(), c.getCodigoPostal(), c.getCorreoElectronico(),
        c.getRegimenFiscal(), c.getUsoFactura(), c.getEstatus(),
        c.getCreatedAt(), c.getUpdatedAt());
  }
}
