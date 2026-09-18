package com.portalfacturacion.adapter.in.web.response;

public record LoginResponse(String token, String tokenType, Long id, String usuario, String tipo) {
  public static LoginResponse bearer(String token, Long id, String usuario, String tipo) {
    return new LoginResponse(token, "Bearer", id, usuario, tipo);
  }
}
