package com.portalfacturacion.application.port.in.factura;

import com.portalfacturacion.application.service.CorreoPreparado;

public interface EnviarFacturaUseCase {
  CorreoPreparado enviarFactura(String correoElectronico, String numeroTicket);

  CorreoPreparado reenviarFactura(String correoElectronico, String numeroTicket);
}
