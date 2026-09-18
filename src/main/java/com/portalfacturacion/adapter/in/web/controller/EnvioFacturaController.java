package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.request.EnviarFacturaRequest;
import com.portalfacturacion.adapter.in.web.request.ReenviarFacturaRequest;
import com.portalfacturacion.adapter.in.web.response.EnvioFacturaResponse;
import com.portalfacturacion.application.port.in.factura.EnviarFacturaUseCase;
import com.portalfacturacion.application.service.CorreoPreparado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/facturacion")
@Tag(name = "Facturacion", description = "Combina ticket de FuDo y cliente local para previsualizar datos de factura")
@Validated
public class EnvioFacturaController {

  private final EnviarFacturaUseCase useCase;

  public EnvioFacturaController(EnviarFacturaUseCase useCase) {
    this.useCase = useCase;
  }

@PostMapping("/reenviar-factura")
  @Operation(summary = "Reenviar correo de factura a partir del numero de ticket",
      description = "Busca la factura por numero de ticket y reenvia el correo al correo "
          + "electronico recibido usando el layout correo_factura.html.")
  public ResponseEntity<EnvioFacturaResponse> reenviar(
      @Valid @RequestBody ReenviarFacturaRequest request) {
    CorreoPreparado enviado = useCase.reenviarFactura(
        request.correoElectronico(), request.numeroTicket());
    return ResponseEntity.accepted().body(
        new EnvioFacturaResponse(
          "Correo reenviado correctamente a " + request.correoElectronico(),
          request.numeroTicket()));
  }

  @PostMapping("/enviar-correo")
  @Operation(summary = "Enviar correo de factura a partir del numero de ticket",
      description = "Recibe el correo destino y el numero de ticket, construye el "
          + "contenido con el layout correo_factura.html y lo envia de forma asincrona. "
          + "Si el ticket no existe retorna 404 y el envio nunca falla el request.")
  public ResponseEntity<EnvioFacturaResponse> enviar(
      @Valid @RequestBody EnviarFacturaRequest request) {
    CorreoPreparado enviado = useCase.enviarFactura(
        request.correoElectronico(), request.numeroTicket());
    return ResponseEntity.accepted().body(
        new EnvioFacturaResponse("Correo enviado correctamente a " + request.correoElectronico(),
          request.numeroTicket()));
  }
}
