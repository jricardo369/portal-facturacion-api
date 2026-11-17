package com.portalfacturacion.application.port.out.persistence;

import com.portalfacturacion.domain.model.Configuracion;
import java.util.List;
import java.util.Optional;

public interface ConfiguracionPort {

  Optional<Configuracion> findByClave(String clave);

  List<Configuracion> findAll();

  Configuracion save(Configuracion configuracion);
}

