package com.portalfacturacion.domain.valueobject;

import com.portalfacturacion.domain.model.TipoUsuario;

public record UsuarioFiltro(String usuario, String nombre, TipoUsuario tipo) {
}
