package com.portalfacturacion.adapter.out.persistence;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.UsuarioEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.repository.UsuarioJpaRepository;
import com.portalfacturacion.adapter.out.persistence.mysql.repository.UsuarioRepositoryAdapter;
import com.portalfacturacion.domain.model.TipoUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioRepositoryAdapterIT {

  @Autowired UsuarioJpaRepository jpa;
  @Autowired UsuarioRepositoryAdapter adapter;

  @Test
  void persistir_y_consultar_conH2() {
    UsuarioEntity e = new UsuarioEntity();
    e.setUsuario("integracion1");
    e.setPassword("HASH");
    e.setNombre("Integracion");
    e.setTipo(UsuarioEntity.TipoEntity.OPERADOR);
    e.setActivo(true);
    e.setCreatedAt(java.time.Instant.now());
    e.setUpdatedAt(java.time.Instant.now());
    jpa.save(e);

    assertTrue(adapter.findByUsuario("integracion1").isPresent());
    assertTrue(adapter.existsByUsuario("integracion1"));
    var page = adapter.findActivos(null, null, null, 0, 20, "id,desc");
    assertTrue(page.totalElements() >= 1);
  }

  @Test
  void findActivos_filtraPorTipo_yExcluyeInactivos() {
    UsuarioEntity activo = new UsuarioEntity();
    activo.setUsuario("filtro_activo");
    activo.setPassword("H");
    activo.setTipo(UsuarioEntity.TipoEntity.ADMIN);
    activo.setActivo(true);
    activo.setCreatedAt(java.time.Instant.now());
    activo.setUpdatedAt(java.time.Instant.now());
    jpa.save(activo);

    UsuarioEntity inactivo = new UsuarioEntity();
    inactivo.setUsuario("filtro_inactivo");
    inactivo.setPassword("H");
    inactivo.setTipo(UsuarioEntity.TipoEntity.ADMIN);
    inactivo.setActivo(false);
    inactivo.setCreatedAt(java.time.Instant.now());
    inactivo.setUpdatedAt(java.time.Instant.now());
    jpa.save(inactivo);

    var page = adapter.findActivos("filtro_", null, TipoUsuario.ADMIN, 0, 20, "id,desc");
    assertTrue(page.content().stream().allMatch(u -> u.getActivo()));
    assertTrue(page.content().stream().noneMatch(u -> u.getUsuario().equals("filtro_inactivo")));
  }
}
