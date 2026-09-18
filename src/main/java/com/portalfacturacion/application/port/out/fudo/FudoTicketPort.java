package com.portalfacturacion.application.port.out.fudo;

import com.portalfacturacion.domain.model.Ticket;
import java.util.Optional;

public interface FudoTicketPort {
  Optional<Ticket> buscarPorFolio(String numeroTicket);
}
