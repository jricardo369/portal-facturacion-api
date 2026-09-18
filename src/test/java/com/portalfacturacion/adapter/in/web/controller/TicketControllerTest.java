package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.application.service.TicketService;
import com.portalfacturacion.domain.exception.TicketNoEncontradoException;
import com.portalfacturacion.domain.model.Ticket;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

  @Autowired MockMvc mvc;
  @MockBean TicketService service;
  @MockBean com.portalfacturacion.adapter.in.security.jwt.JwtAuthenticationFilter jwtFilter;
  @MockBean com.portalfacturacion.application.port.out.security.TokenProviderPort tokenProvider;

  private Ticket ticket() {
    return new Ticket("123", new BigDecimal("1350.50"), new BigDecimal("1350.50"), null,
        "CLOSED", "EAT-IN", null, null, "Paula", "Mesa 1", null, true, List.of(), List.of());
  }

  @Test
  void obtenerTicket_ok_200_publico() throws Exception {
    when(service.obtenerTicket("123")).thenReturn(ticket());
    mvc.perform(get("/api/v1/tickets/123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.numeroTicket").value("123"))
        .andExpect(jsonPath("$.total").value(1350.50))
        .andExpect(jsonPath("$.facturable").value(true));
  }

  @Test
  void obtenerTicket_noEncontrado_404() throws Exception {
    when(service.obtenerTicket("999")).thenThrow(new TicketNoEncontradoException("Ticket no encontrado con folio 999"));
    mvc.perform(get("/api/v1/tickets/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void obtenerTicket_formatoInvalido_400() throws Exception {
    mvc.perform(get("/api/v1/tickets/!!!"))
        .andExpect(status().isBadRequest());
  }
}
