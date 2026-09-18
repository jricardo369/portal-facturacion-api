package com.portalfacturacion.adapter.out.security;

import com.portalfacturacion.configuration.JwtProperties;
import com.portalfacturacion.adapter.out.security.jwt.JwtTokenAdapter;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenAdapterTest {

  JwtTokenAdapter adapter;

  @BeforeEach
  void setUp() {
    JwtProperties props = new JwtProperties();
    props.setSecret("test-secret-key-with-at-least-32-chars-0123456789!!");
    props.setExpirationMs(600000);
    props.setIssuer("portal-facturacion-test");
    props.setAudience("portal-facturacion-api-test");
    adapter = new JwtTokenAdapter(props);
  }

  @Test
  void generar_y_validar_ok() {
    Usuario u = new Usuario(7L, "ana", "x", "Ana", null, null, TipoUsuario.ADMIN, true, null, null);
    String token = adapter.generar(u);
    assertNotNull(token);
    assertTrue(adapter.valido(token));
    assertEquals("ana", adapter.obtenerUsuario(token));
    assertEquals(7L, adapter.obtenerId(token));
    assertEquals("ADMIN", adapter.obtenerTipo(token));
  }

  @Test
  void token_invalido_noValido() {
    assertFalse(adapter.valido("invalido.token.valor"));
  }

  @Test
  void token_con_otro_secret_noValido() {
    Usuario u = new Usuario(1L, "bob", "x", "Bob", null, null, TipoUsuario.OPERADOR, true, null, null);
    String token = adapter.generar(u);
    JwtProperties otras = new JwtProperties();
    otras.setSecret("otra-clave-distinta-con-al-menos-32-caracteres-1234");
    otras.setExpirationMs(600000);
    otras.setIssuer("portal-facturacion-test");
    otras.setAudience("portal-facturacion-api-test");
    assertFalse(new JwtTokenAdapter(otras).valido(token));
  }
}
