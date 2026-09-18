package com.portalfacturacion.application.service;

import com.portalfacturacion.domain.model.Usuario;

public record AuthResult(String token, Usuario usuario) {
}
