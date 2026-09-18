package com.portalfacturacion.domain.model;

import java.math.BigDecimal;

public class TicketPago {
  private String id;
  private String metodo;
  private BigDecimal monto;

  public TicketPago() {
  }

  public TicketPago(String id, String metodo, BigDecimal monto) {
    this.id = id;
    this.metodo = metodo;
    this.monto = monto;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getMetodo() { return metodo; }
  public void setMetodo(String metodo) { this.metodo = metodo; }
  public BigDecimal getMonto() { return monto; }
  public void setMonto(BigDecimal monto) { this.monto = monto; }
}
