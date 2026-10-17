package com.portalfacturacion.application.port.out.facturapi;

import com.portalfacturacion.domain.model.FacturaTimbrada;
import com.portalfacturacion.domain.model.ResultadoCancelacion;
import com.portalfacturacion.domain.model.SolicitudTimbrado;
import java.util.Optional;

public interface FacturapiTimbradoPort {

  FacturaTimbrada timbrar(SolicitudTimbrado solicitud);

  ResultadoCancelacion cancelar(String facturapiId, String motivo, String sustitucion);

  Optional<FacturaTimbrada> consultar(String facturapiId);

  byte[] obtenerXml(String facturapiId);

  byte[] obtenerPdf(String facturapiId);

  byte[] obtenerZip(String facturapiId);
}
