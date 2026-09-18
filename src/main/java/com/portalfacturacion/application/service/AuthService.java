package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.out.persistence.UsuarioRepositoryPort;
import com.portalfacturacion.application.port.out.security.PasswordEncoderPort;
import com.portalfacturacion.application.port.out.security.TokenProviderPort;
import com.portalfacturacion.domain.exception.CredencialesInvalidasException;
import com.portalfacturacion.domain.exception.UsuarioInactivoException;
import com.portalfacturacion.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UsuarioRepositoryPort repository;
  private final PasswordEncoderPort passwordEncoder;
  private final TokenProviderPort tokenProvider;

  public AuthService(UsuarioRepositoryPort repository, PasswordEncoderPort passwordEncoder,
                     TokenProviderPort tokenProvider) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
  }

  @Transactional(readOnly = true)
  public AuthResult login(String usuario, String password) {
    Usuario encontrado = repository.findByUsuario(usuario)
        .orElseThrow(() -> new CredencialesInvalidasException("Credenciales invalidas"));
    if (!Boolean.TRUE.equals(encontrado.getActivo())) {
      throw new UsuarioInactivoException("Usuario inactivo");
    }
    if (!passwordEncoder.matches(password, encontrado.getPassword())) {
      throw new CredencialesInvalidasException("Credenciales invalidas");
    }
    String token = tokenProvider.generar(encontrado);
    return new AuthResult(token, encontrado);
  }
}
