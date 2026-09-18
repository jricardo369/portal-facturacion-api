package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.UsuarioRepositoryPort;
import com.portalfacturacion.application.port.out.security.PasswordEncoderPort;
import com.portalfacturacion.domain.exception.UsuarioDuplicadoException;
import com.portalfacturacion.domain.exception.UsuarioNoEncontradoException;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

  @Mock UsuarioRepositoryPort repository;
  @Mock PasswordEncoderPort passwordEncoder;
  UsuarioService service;

  @BeforeEach
  void setUp() {
    service = new UsuarioService(repository, passwordEncoder);
  }

  private Usuario nuevo() {
    return new Usuario(null, "juan", "secreto123", "Juan", "Calle 1", "555", TipoUsuario.OPERADOR, true, null, null);
  }

  @Test
  void crear_ok_codificaPassword() {
    when(repository.existsByUsuario("juan")).thenReturn(false);
    when(passwordEncoder.encode("secreto123")).thenReturn("HASH");
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
    Usuario creado = service.crear(nuevo());
    assertEquals("HASH", creado.getPassword());
    assertTrue(creado.getActivo());
  }

  @Test
  void crear_duplicado_lanza409() {
    when(repository.existsByUsuario("juan")).thenReturn(true);
    assertThrows(UsuarioDuplicadoException.class, () -> service.crear(nuevo()));
  }

  @Test
  void obtener_ok() {
    Usuario u = nuevo();
    u.setId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(u));
    assertEquals("juan", service.obtenerPorId(1L).getUsuario());
  }

  @Test
  void obtener_inexistente_lanza404() {
    when(repository.findById(99L)).thenReturn(Optional.empty());
    assertThrows(UsuarioNoEncontradoException.class, () -> service.obtenerPorId(99L));
  }

  @Test
  void obtener_inactivo_lanza404() {
    Usuario u = nuevo();
    u.setActivo(false);
    when(repository.findById(1L)).thenReturn(Optional.of(u));
    assertThrows(UsuarioNoEncontradoException.class, () -> service.obtenerPorId(1L));
  }

  @Test
  void actualizar_noReemplazaPasswordVacio() {
    Usuario actual = nuevo();
    actual.setId(1L);
    actual.setPassword("HASH_OLD");
    when(repository.findById(1L)).thenReturn(Optional.of(actual));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
    Usuario cambios = new Usuario(1L, "juan", "", "Juan Nuevo", null, null, null, true, null, null);
    Usuario res = service.actualizar(1L, cambios);
    assertEquals("HASH_OLD", res.getPassword());
    assertEquals("Juan Nuevo", res.getNombre());
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void actualizar_conPassword_codifica() {
    Usuario actual = nuevo();
    actual.setId(1L);
    actual.setPassword("OLD");
    when(repository.findById(1L)).thenReturn(Optional.of(actual));
    when(passwordEncoder.encode("nuevoSecreto1")).thenReturn("NEW_HASH");
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
    Usuario cambios = new Usuario(1L, "juan", "nuevoSecreto1", "Juan", null, null, null, true, null, null);
    assertEquals("NEW_HASH", service.actualizar(1L, cambios).getPassword());
  }

  @Test
  void actualizar_duplicado_lanza409() {
    Usuario actual = nuevo();
    actual.setId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(actual));
    when(repository.existsByUsuario("otro")).thenReturn(true);
    Usuario cambios = new Usuario(1L, "otro", null, "X", null, null, null, true, null, null);
    assertThrows(UsuarioDuplicadoException.class, () -> service.actualizar(1L, cambios));
  }

  @Test
  void eliminar_bajaLogica() {
    Usuario actual = nuevo();
    actual.setId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(actual));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
    service.eliminar(1L);
    assertFalse(actual.getActivo());
  }

  @Test
  void listar_delegaAlPuerto() {
    PagedResult<Usuario> page = new PagedResult<>(List.of(), 0, 20, 0, 0);
    when(repository.findActivos(any(), any(), any(), anyInt(), anyInt(), any())).thenReturn(page);
    assertEquals(0, service.listar(null, null, null, 0, 20, "id,desc").totalElements());
  }
}
