package com.portalfacturacion.adapter.out.persistence.mysql.mapper;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.ConfiguracionEntity;
import com.portalfacturacion.domain.model.Configuracion;

public final class ConfiguracionMapper {

  private ConfiguracionMapper() {
  }

  public static Configuracion toDomain(ConfiguracionEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Configuracion(entity.getId(), entity.getClave(), entity.getValor(),
        entity.getDescripcion(), entity.getActivo(), entity.getCreatedAt(), entity.getUpdatedAt());
  }

  public static ConfiguracionEntity toEntity(Configuracion domain) {
    if (domain == null) {
      return null;
    }
    ConfiguracionEntity entity = new ConfiguracionEntity();
    entity.setId(domain.id());
    entity.setClave(domain.clave());
    entity.setValor(domain.valor());
    entity.setDescripcion(domain.descripcion());
    entity.setActivo(domain.activo() != null ? domain.activo() : true);
    entity.setCreatedAt(domain.createdAt());
    entity.setUpdatedAt(domain.updatedAt());
    return entity;
  }
}

