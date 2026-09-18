package com.portalfacturacion.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portalfacturacion.adapter.in.web.request.LoginRequest;
import com.portalfacturacion.application.service.AuthResult;
import com.portalfacturacion.application.service.AuthService;
import com.portalfacturacion.domain.exception.CredencialesInvalidasException;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @MockBean AuthService authService;
  @MockBean com.portalfacturacion.adapter.in.security.jwt.JwtAuthenticationFilter jwtFilter;
  @MockBean com.portalfacturacion.application.port.out.security.TokenProviderPort tokenProvider;

  @Test
  void login_exitoso_200() throws Exception {
    Usuario u = new Usuario(1L, "admin", "HASH", "Admin", null, null, TipoUsuario.ADMIN, true, null, null);
    when(authService.login(anyString(), anyString())).thenReturn(new AuthResult("JWT-TOKEN", u));
    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(new LoginRequest("admin", "secret123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("JWT-TOKEN"))
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void login_credencialesInvalidas_401() throws Exception {
    when(authService.login(anyString(), anyString())).thenThrow(new CredencialesInvalidasException("Credenciales invalidas"));
    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(new LoginRequest("admin", "bad"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_validacion_400() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuario\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
