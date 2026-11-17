package com.portalfacturacion.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class TicketFactura {
  private Long id;
  private String numeroTicket;
  private Instant fechaEmision;
  private String hora;
  private BigDecimal subtotal;
  private BigDecimal iva;
  private BigDecimal isr;
  private BigDecimal cedular;
  private BigDecimal total;

  public TicketFactura() {
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getNumeroTicket() { return numeroTicket; }
  public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }
  public Instant getFechaEmision() { return fechaEmision; }
  public void setFechaEmision(Instant fechaEmision) { this.fechaEmision = fechaEmision; }
  public String getHora() { return hora; }
  public void setHora(String hora) { this.hora = hora; }
  public BigDecimal getSubtotal() { return subtotal; }
  public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
  public BigDecimal getIva() { return iva; }
  public void setIva(BigDecimal iva) { this.iva = iva; }
  public BigDecimal getIsr() { return isr; }
  public void setIsr(BigDecimal isr) { this.isr = isr; }
  public BigDecimal getCedular() { return cedular; }
  public void setCedular(BigDecimal cedular) { this.cedular = cedular; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
}
