package com.portalfacturacion.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portalfacturacion.adapter.out.persistence.mysql.entity.UsuarioEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.repository.UsuarioJpaRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioFlujoIT {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired UsuarioJpaRepository jpa;
  @Autowired PasswordEncoder encoder;

  @BeforeEach
  void seed() {
    jpa.deleteAll();
    jpa.save(entity("admin", "admin1234", UsuarioEntity.TipoEntity.ADMIN, true));
    jpa.save(entity("operador", "operador123", UsuarioEntity.TipoEntity.OPERADOR, true));
    jpa.save(entity("inactivo", "inactivo123", UsuarioEntity.TipoEntity.OPERADOR, false));
  }

  private UsuarioEntity entity(String user, String pass, UsuarioEntity.TipoEntity tipo, boolean activo) {
    UsuarioEntity e = new UsuarioEntity();
    e.setUsuario(user);
    e.setPassword(encoder.encode(pass));
    e.setNombre(user);
    e.setTipo(tipo);
    e.setActivo(activo);
    e.setCreatedAt(Instant.now());
    e.setUpdatedAt(Instant.now());
    return e;
  }

  private String login(String user, String pass) throws Exception {
    String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"" + user + "\",\"password\":\"" + pass + "\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    JsonNode node = mapper.readTree(body);
    return node.get("token").asText();
  }

  @Test
  void login_exitoso() throws Exception {
    login("admin", "admin1234");
  }

  @Test
  void login_credencialesInvalidas_401() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"admin\",\"password\":\"mal\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_inactivo_401() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"inactivo\",\"password\":\"inactivo123\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void acceso_sinToken_401() throws Exception {
    mvc.perform(get("/api/v1/usuarios")).andExpect(status().isUnauthorized());
  }

  @Test
  void acceso_tokenInvalido_401() throws Exception {
    mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer invalido"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void crud_completo_y_bajaLogica() throws Exception {
    String adminToken = login("admin", "admin1234");

    String creado = mvc.perform(post("/api/v1/usuarios").header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"nuevo1\",\"password\":\"secreto123\",\"nombre\":\"Nuevo\",\"tipo\":\"OPERADOR\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.password").doesNotExist())
        .andReturn().getResponse().getContentAsString();
    long id = mapper.readTree(creado).get("id").asLong();

    mvc.perform(get("/api/v1/usuarios/" + id).header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario").value("nuevo1"));

    mvc.perform(put("/api/v1/usuarios/" + id).header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"nuevo1\",\"nombre\":\"Nuevo Editado\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Nuevo Editado"));

    mvc.perform(post("/api/v1/usuarios").header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"nuevo1\",\"password\":\"secreto123\"}"))
        .andExpect(status().isConflict());

    mvc.perform(get("/api/v1/usuarios/99999").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isNotFound());

    mvc.perform(delete("/api/v1/usuarios/" + id).header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/v1/usuarios/" + id).header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isNotFound());

    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"nuevo1\",\"password\":\"secreto123\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void autorizacion_operador_noPuedeCrear_403() throws Exception {
    String opToken = login("operador", "operador123");
    mvc.perform(post("/api/v1/usuarios").header("Authorization", "Bearer " + opToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"x1\",\"password\":\"secreto123\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + opToken))
        .andExpect(status().isOk());
  }
}
