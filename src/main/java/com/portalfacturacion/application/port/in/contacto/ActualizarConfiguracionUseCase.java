package com.portalfacturacion.application.port.in.contacto;

import com.portalfacturacion.domain.model.Configuracion;

public interface ActualizarConfiguracionUseCase {

  Configuracion actualizar(String clave, String valor, String descripcion, Boolean activo);
}
