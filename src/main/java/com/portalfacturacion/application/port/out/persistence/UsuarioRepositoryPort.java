package com.portalfacturacion.application.port.out.persistence;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {
  Usuario save(Usuario usuario);
  Optional<Usuario> findById(Long id);
  Optional<Usuario> findByUsuario(String usuario);
  boolean existsByUsuario(String usuario);
  PagedResult<Usuario> findActivos(String usuario, String nombre, TipoUsuario tipo, int page, int size, String sort);
}
