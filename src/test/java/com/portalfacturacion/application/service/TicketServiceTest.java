package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.out.fudo.FudoTicketPort;
import com.portalfacturacion.domain.exception.TicketNoEncontradoException;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.domain.model.TicketPago;
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

  @Test
  void obtenerTicket_estadoNoCerradoSinPagos_rechaza() {
    Ticket abierto = new Ticket("456", new BigDecimal("100.00"), new BigDecimal("100.00"), null,
        "IN-COURSE", "EAT-IN", null, null, "Paula", "Mesa 1", null, false, List.of(), List.of());
    when(fudoTicketPort.buscarPorFolio("456")).thenReturn(Optional.of(abierto));

    assertThatThrownBy(() -> service.obtenerTicket("456"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("El ticket no tiene el estatus correcto para su facturación");
  }

  @Test
  void obtenerTicket_estadoNoCerradoConPagos_permite() {
    Ticket abierto = new Ticket("789", new BigDecimal("100.00"), new BigDecimal("100.00"), null,
        "IN-COURSE", "EAT-IN", null, null, "Paula", "Mesa 1", null, false, List.of(),
        List.of(new TicketPago("20", "Cash", new BigDecimal("100.00"))));
    when(fudoTicketPort.buscarPorFolio("789")).thenReturn(Optional.of(abierto));

    assertThat(service.obtenerTicket("789").getNumeroTicket()).isEqualTo("789");
  }
}
