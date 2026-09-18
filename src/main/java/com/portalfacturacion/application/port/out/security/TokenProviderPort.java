package com.portalfacturacion.application.port.out.security;

import com.portalfacturacion.domain.model.Usuario;

public interface TokenProviderPort {
  String generar(Usuario usuario);
  boolean valido(String token);
  String obtenerUsuario(String token);
  Long obtenerId(String token);
  String obtenerTipo(String token);
}
