package com.portalfacturacion.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DatosFactura {
  private final Ticket ticket;
  private final List<Ticket> tickets;
  private final Cliente cliente;

  public DatosFactura(Ticket ticket, Cliente cliente) {
    this.ticket = ticket;
    this.tickets = ticket != null ? List.of(ticket) : List.of();
    this.cliente = cliente;
  }

  public DatosFactura(List<Ticket> tickets, Cliente cliente) {
    this.tickets = tickets == null || tickets.isEmpty() ? List.of() : List.copyOf(tickets);
    this.ticket = this.tickets.isEmpty() ? null : consolidateTickets(this.tickets);
    this.cliente = cliente;
  }

  private static Ticket consolidateTickets(List<Ticket> list) {
    if (list.size() == 1) {
      return list.get(0);
    }
    Ticket first = list.get(0);
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal iva = BigDecimal.ZERO;
    BigDecimal isr = BigDecimal.ZERO;
    BigDecimal cedular = BigDecimal.ZERO;
    BigDecimal impuestos = BigDecimal.ZERO;
    List<TicketItem> allItems = new ArrayList<>();
    List<TicketPago> allPagos = new ArrayList<>();

    for (Ticket t : list) {
      if (t.getSubtotal() != null) subtotal = subtotal.add(t.getSubtotal());
      if (t.getTotal() != null) total = total.add(t.getTotal());
      if (t.getIva() != null) iva = iva.add(t.getIva());
      if (t.getIsr() != null) isr = isr.add(t.getIsr());
      if (t.getCedular() != null) cedular = cedular.add(t.getCedular());
      if (t.getImpuestos() != null) impuestos = impuestos.add(t.getImpuestos());
      if (t.getItems() != null) allItems.addAll(t.getItems());
      if (t.getPagos() != null) allPagos.addAll(t.getPagos());
    }

    Ticket consolidated = new Ticket();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) sb.append(", ");
      sb.append(list.get(i).getNumeroTicket());
    }
    consolidated.setNumeroTicket(sb.toString());
    consolidated.setSubtotal(subtotal);
    consolidated.setTotal(total);
    consolidated.setIva(iva);
    consolidated.setIsr(isr);
    consolidated.setCedular(cedular);
    consolidated.setImpuestos(impuestos);
    consolidated.setFechaEmision(first.getFechaEmision());
    consolidated.setFechaCierre(first.getFechaCierre());
    consolidated.setTipoVenta(first.getTipoVenta());
    consolidated.setItems(allItems);
    consolidated.setPagos(allPagos);
    return consolidated;
  }

  public Ticket getTicket() { return ticket; }
  public List<Ticket> getTickets() { return tickets; }
  public Cliente getCliente() { return cliente; }
}
