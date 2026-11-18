package com.portalfacturacion.application.port.in.factura;

import com.portalfacturacion.application.service.CorreoPreparado;
import java.math.BigDecimal;

public interface EnviarFacturaUseCase {
  CorreoPreparado enviarFactura(String correoElectronico, String numeroTicket);

  CorreoPreparado reenviarFactura(String correoElectronico, String numeroTicket,
      String fecha, BigDecimal total);

  default CorreoPreparado reenviarFactura(String correoElectronico, String numeroTicket) {
    return reenviarFactura(correoElectronico, numeroTicket, null, null);
  }
}
