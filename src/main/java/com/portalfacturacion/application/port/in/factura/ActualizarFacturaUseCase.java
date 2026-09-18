package com.portalfacturacion.application.port.in.factura;

import com.portalfacturacion.domain.model.Factura;

public interface ActualizarFacturaUseCase {
  Factura actualizarPorId(Long idFactura, Factura cambios);
}
