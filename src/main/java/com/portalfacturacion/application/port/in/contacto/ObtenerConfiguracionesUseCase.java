package com.portalfacturacion.application.port.in.contacto;

import com.portalfacturacion.domain.model.Configuracion;
import java.util.List;

public interface ObtenerConfiguracionesUseCase {

  List<Configuracion> obtenerTodas();
}
