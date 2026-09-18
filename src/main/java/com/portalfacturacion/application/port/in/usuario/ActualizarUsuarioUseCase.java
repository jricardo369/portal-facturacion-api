package com.portalfacturacion.application.port.in.usuario;

import com.portalfacturacion.domain.model.Usuario;

public interface ActualizarUsuarioUseCase {
  Usuario actualizar(Long id, Usuario usuario);
}
