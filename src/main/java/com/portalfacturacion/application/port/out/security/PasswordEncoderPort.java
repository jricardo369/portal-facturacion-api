package com.portalfacturacion.application.port.out.security;

public interface PasswordEncoderPort {
  String encode(String rawPassword);
  boolean matches(String rawPassword, String encodedPassword);
}
