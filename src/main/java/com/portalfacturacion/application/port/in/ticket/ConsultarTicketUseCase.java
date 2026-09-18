package com.portalfacturacion.application.port.in.ticket;

import com.portalfacturacion.domain.model.Ticket;

public interface ConsultarTicketUseCase {
  Ticket obtenerTicket(String numeroTicket);
}
