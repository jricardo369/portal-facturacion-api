package com.portalfacturacion.domain.model;

public class DatosFactura {
  private final Ticket ticket;
  private final Cliente cliente;

  public DatosFactura(Ticket ticket, Cliente cliente) {
    this.ticket = ticket;
    this.cliente = cliente;
  }

  public Ticket getTicket() { return ticket; }
  public Cliente getCliente() { return cliente; }
}
