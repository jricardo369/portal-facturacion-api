package com.portalfacturacion.adapter.out.persistence.mysql.mapper;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.UsuarioEntity;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;

public final class UsuarioMapper {

  private UsuarioMapper() {
  }

  public static Usuario toDomain(UsuarioEntity e) {
    if (e == null) {
      return null;
    }
    TipoUsuario tipo = e.getTipo() == null ? null : TipoUsuario.valueOf(e.getTipo().name());
    return new Usuario(e.getId(), e.getUsuario(), e.getPassword(), e.getNombre(),
        e.getDireccion(), e.getTelefono(), tipo, e.getActivo(), e.getCreatedAt(), e.getUpdatedAt());
  }

  public static UsuarioEntity toEntity(Usuario d) {
    UsuarioEntity e = new UsuarioEntity();
    e.setId(d.getId());
    e.setUsuario(d.getUsuario());
    e.setPassword(d.getPassword());
    e.setNombre(d.getNombre());
    e.setDireccion(d.getDireccion());
    e.setTelefono(d.getTelefono());
    e.setTipo(d.getTipo() == null ? null : UsuarioEntity.TipoEntity.valueOf(d.getTipo().name()));
    e.setActivo(d.getActivo() == null ? Boolean.TRUE : d.getActivo());
    e.setCreatedAt(d.getCreatedAt());
    e.setUpdatedAt(d.getUpdatedAt());
    return e;
  }
}
