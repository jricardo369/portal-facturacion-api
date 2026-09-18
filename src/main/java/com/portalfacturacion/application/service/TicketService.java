package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.ticket.ConsultarTicketUseCase;
import com.portalfacturacion.application.port.out.fudo.FudoTicketPort;
import com.portalfacturacion.domain.exception.TicketNoEncontradoException;
import com.portalfacturacion.domain.model.Ticket;
import org.springframework.stereotype.Service;

@Service
public class TicketService implements ConsultarTicketUseCase {

  private final FudoTicketPort fudoTicketPort;

  public TicketService(FudoTicketPort fudoTicketPort) {
    this.fudoTicketPort = fudoTicketPort;
  }

  @Override
  public Ticket obtenerTicket(String numeroTicket) {
    String folio = numeroTicket == null ? "" : numeroTicket.trim();
    if (folio.isEmpty()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio");
    }
    return fudoTicketPort.buscarPorFolio(folio)
        .orElseThrow(() -> new TicketNoEncontradoException("Ticket no encontrado con folio " + folio));
  }
}
