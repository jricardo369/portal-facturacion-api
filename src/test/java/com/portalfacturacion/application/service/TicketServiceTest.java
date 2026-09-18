package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.out.fudo.FudoTicketPort;
import com.portalfacturacion.domain.exception.TicketNoEncontradoException;
import com.portalfacturacion.domain.model.Ticket;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

  @Mock FudoTicketPort fudoTicketPort;
  @InjectMocks TicketService service;

  private Ticket ticket() {
    return new Ticket("123", new BigDecimal("100.00"), new BigDecimal("100.00"), null,
        "CLOSED", "EAT-IN", null, null, "Paula", "Mesa 1", null, true, List.of(), List.of());
  }

  @Test
  void obtenerTicket_ok() {
    when(fudoTicketPort.buscarPorFolio("123")).thenReturn(Optional.of(ticket()));
    assertThat(service.obtenerTicket("123").getNumeroTicket()).isEqualTo("123");
  }

  @Test
  void obtenerTicket_noEncontrado_404() {
    when(fudoTicketPort.buscarPorFolio("999")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.obtenerTicket("999"))
        .isInstanceOf(TicketNoEncontradoException.class);
  }

  @Test
  void obtenerTicket_folioVacio_400() {
    assertThatThrownBy(() -> service.obtenerTicket("  "))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
