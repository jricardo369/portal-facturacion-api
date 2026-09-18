package com.portalfacturacion.adapter.out.security;

import com.portalfacturacion.application.port.out.security.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordAdapter implements PasswordEncoderPort {

  private final PasswordEncoder delegate;

  public BCryptPasswordAdapter(PasswordEncoder delegate) {
    this.delegate = delegate;
  }

  @Override
  public String encode(String rawPassword) {
    return delegate.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return delegate.matches(rawPassword, encodedPassword);
  }
}
