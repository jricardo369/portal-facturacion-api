package com.portalfacturacion.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portalfacturacion.adapter.in.web.request.CrearUsuarioRequest;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.service.UsuarioService;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @MockBean UsuarioService service;
  @MockBean com.portalfacturacion.adapter.in.security.jwt.JwtAuthenticationFilter jwtFilter;
  @MockBean com.portalfacturacion.application.port.out.security.TokenProviderPort tokenProvider;

  private Usuario usuario() {
    return new Usuario(1L, "juan", "HASH", "Juan Perez", "Calle", "555",
        TipoUsuario.OPERADOR, true, Instant.now(), Instant.now());
  }

  @Test
  void listar_ok_nuncaDevuelvePassword() throws Exception {
    when(service.listar(any(), any(), any(), any(int.class), any(int.class), any()))
        .thenReturn(new PagedResult<>(List.of(usuario()), 0, 20, 1, 1));
    mvc.perform(get("/api/v1/usuarios"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].usuario").value("juan"))
        .andExpect(jsonPath("$.content[0].password").doesNotExist());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crear_ok_201_sinPassword() throws Exception {
    var req = new CrearUsuarioRequest("nuevo", "secreto123", "Nuevo", null, null, TipoUsuario.OPERADOR);
    when(service.crear(any())).thenReturn(usuario());
    mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crear_datosInvalidos_400() throws Exception {
    mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"\",\"password\":\"x\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void eliminar_ok_204() throws Exception {
    mvc.perform(delete("/api/v1/usuarios/1")).andExpect(status().isNoContent());
  }
}
