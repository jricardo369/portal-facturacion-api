package com.portalfacturacion.application.port.in.usuario;

import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;

public interface ConsultarUsuarioUseCase {
  Usuario obtenerPorId(Long id);
  PagedResult<Usuario> listar(String usuario, String nombre, TipoUsuario tipo, int page, int size, String sort);
}
