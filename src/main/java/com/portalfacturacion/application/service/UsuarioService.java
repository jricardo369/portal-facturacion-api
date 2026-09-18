package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.usuario.ActualizarUsuarioUseCase;
import com.portalfacturacion.application.port.in.usuario.ConsultarUsuarioUseCase;
import com.portalfacturacion.application.port.in.usuario.CrearUsuarioUseCase;
import com.portalfacturacion.application.port.in.usuario.EliminarUsuarioUseCase;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.UsuarioRepositoryPort;
import com.portalfacturacion.application.port.out.security.PasswordEncoderPort;
import com.portalfacturacion.domain.exception.UsuarioDuplicadoException;
import com.portalfacturacion.domain.exception.UsuarioNoEncontradoException;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService implements CrearUsuarioUseCase, ConsultarUsuarioUseCase,
    ActualizarUsuarioUseCase, EliminarUsuarioUseCase {

  private final UsuarioRepositoryPort repository;
  private final PasswordEncoderPort passwordEncoder;

  public UsuarioService(UsuarioRepositoryPort repository, PasswordEncoderPort passwordEncoder) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public Usuario crear(Usuario usuario) {
    if (repository.existsByUsuario(usuario.getUsuario())) {
      throw new UsuarioDuplicadoException("El usuario '" + usuario.getUsuario() + "' ya existe");
    }
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    usuario.setActivo(true);
    usuario.setCreatedAt(Instant.now());
    usuario.setUpdatedAt(Instant.now());
    if (usuario.getTipo() == null) {
      usuario.setTipo(TipoUsuario.OPERADOR);
    }
    return repository.save(usuario);
  }

  @Override
  @Transactional(readOnly = true)
  public Usuario obtenerPorId(Long id) {
    return repository.findById(id)
        .filter(u -> Boolean.TRUE.equals(u.getActivo()))
        .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con id " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResult<Usuario> listar(String usuario, String nombre, TipoUsuario tipo, int page, int size, String sort) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    return repository.findActivos(usuario, nombre, tipo, safePage, safeSize, sort);
  }

  @Override
  @Transactional
  public Usuario actualizar(Long id, Usuario cambios) {
    Usuario actual = obtenerPorId(id);
    if (!actual.getUsuario().equalsIgnoreCase(cambios.getUsuario())
        && repository.existsByUsuario(cambios.getUsuario())) {
      throw new UsuarioDuplicadoException("El usuario '" + cambios.getUsuario() + "' ya existe");
    }
    actual.setUsuario(cambios.getUsuario());
    actual.setNombre(cambios.getNombre());
    actual.setDireccion(cambios.getDireccion());
    actual.setTelefono(cambios.getTelefono());
    if (cambios.getTipo() != null) {
      actual.setTipo(cambios.getTipo());
    }
    if (cambios.getPassword() != null && !cambios.getPassword().isBlank()) {
      actual.setPassword(passwordEncoder.encode(cambios.getPassword()));
    }
    actual.setUpdatedAt(Instant.now());
    return repository.save(actual);
  }

  @Override
  @Transactional
  public void eliminar(Long id) {
    Usuario actual = obtenerPorId(id);
    actual.setActivo(false);
    actual.setUpdatedAt(Instant.now());
    repository.save(actual);
  }
}
