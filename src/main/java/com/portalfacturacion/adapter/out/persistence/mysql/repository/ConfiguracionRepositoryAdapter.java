package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.ConfiguracionEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.mapper.ConfiguracionMapper;
import com.portalfacturacion.application.port.out.persistence.ConfiguracionPort;
import com.portalfacturacion.domain.model.Configuracion;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionRepositoryAdapter implements ConfiguracionPort {

  private final ConfiguracionJpaRepository jpa;

  public ConfiguracionRepositoryAdapter(ConfiguracionJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<Configuracion> findByClave(String clave) {
    return jpa.findByClave(clave).map(ConfiguracionMapper::toDomain);
  }

  @Override
  public List<Configuracion> findAll() {
    return jpa.findAll().stream().map(ConfiguracionMapper::toDomain).toList();
  }

  @Override
  public Configuracion save(Configuracion configuracion) {
    ConfiguracionEntity entity = ConfiguracionMapper.toEntity(configuracion);
    if (entity.getClave() != null) {
      jpa.findByClave(entity.getClave()).ifPresent(existente -> {
        entity.setId(existente.getId());
        entity.setCreatedAt(existente.getCreatedAt());
      });
    }
    ConfiguracionEntity saved = jpa.save(entity);
    return ConfiguracionMapper.toDomain(saved);
  }
}

