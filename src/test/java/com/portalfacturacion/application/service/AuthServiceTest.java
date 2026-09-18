package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.out.persistence.UsuarioRepositoryPort;
import com.portalfacturacion.application.port.out.security.PasswordEncoderPort;
import com.portalfacturacion.application.port.out.security.TokenProviderPort;
import com.portalfacturacion.domain.exception.CredencialesInvalidasException;
import com.portalfacturacion.domain.exception.UsuarioInactivoException;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UsuarioRepositoryPort repository;
  @Mock PasswordEncoderPort passwordEncoder;
  @Mock TokenProviderPort tokenProvider;

  private AuthService service() {
    return new AuthService(repository, passwordEncoder, tokenProvider);
  }

  private Usuario usuario(boolean activo) {
    return new Usuario(1L, "admin", "HASH", "Admin", null, null, TipoUsuario.ADMIN, activo, null, null);
  }

  @Test
  void login_exitoso_devuelveToken() {
    when(repository.findByUsuario("admin")).thenReturn(Optional.of(usuario(true)));
    when(passwordEncoder.matches("plain123", "HASH")).thenReturn(true);
    when(tokenProvider.generar(any())).thenReturn("JWT");
    AuthResult r = service().login("admin", "plain123");
    assertEquals("JWT", r.token());
  }

  @Test
  void login_credencialesInvalidas() {
    when(repository.findByUsuario("admin")).thenReturn(Optional.of(usuario(true)));
    when(passwordEncoder.matches("bad", "HASH")).thenReturn(false);
    assertThrows(CredencialesInvalidasException.class, () -> service().login("admin", "bad"));
  }

  @Test
  void login_usuarioInexistente_401() {
    when(repository.findByUsuario("x")).thenReturn(Optional.empty());
    assertThrows(CredencialesInvalidasException.class, () -> service().login("x", "y"));
  }

  @Test
  void login_inactivo_rechazado() {
    when(repository.findByUsuario("admin")).thenReturn(Optional.of(usuario(false)));
    assertThrows(UsuarioInactivoException.class, () -> service().login("admin", "plain123"));
  }
}
