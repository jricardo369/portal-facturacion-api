package com.portalfacturacion.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Factura {
  private Long idFactura;
  private String folio;
  private String serie;
  private Instant fecha;
  private String cliente;
  private String rfc;
  private String razonSocial;
  private String noTicket;
  private BigDecimal subtotal;
  private BigDecimal impuesto;
  private BigDecimal iva;
  private BigDecimal isr;
  private BigDecimal cedular;
  private BigDecimal total;
  private String estatus;
  private String uuid;
  private String facturapiId;
  private String cancellationStatus;
  private Instant createdAt;
  private Instant updatedAt;
  private List<TicketFactura> tickets = List.of();

  public Factura() {
  }

  public Factura(Long idFactura, String folio, String serie, Instant fecha,
                 String cliente, String rfc, String razonSocial, String noTicket,
                 BigDecimal subtotal, BigDecimal impuesto, BigDecimal total,
                 String estatus, String uuid, String facturapiId, String cancellationStatus,
                 Instant createdAt, Instant updatedAt) {
    this.idFactura = idFactura;
    this.folio = folio;
    this.serie = serie;
    this.fecha = fecha;
    this.cliente = cliente;
    this.rfc = rfc;
    this.razonSocial = razonSocial;
    this.noTicket = noTicket;
    this.subtotal = subtotal;
    this.impuesto = impuesto;
    this.total = total;
    this.estatus = estatus;
    this.uuid = uuid;
    this.facturapiId = facturapiId;
    this.cancellationStatus = cancellationStatus;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getIdFactura() { return idFactura; }
  public void setIdFactura(Long idFactura) { this.idFactura = idFactura; }
  public String getFolio() { return folio; }
  public void setFolio(String folio) { this.folio = folio; }
  public String getSerie() { return serie; }
  public void setSerie(String serie) { this.serie = serie; }
  public Instant getFecha() { return fecha; }
  public void setFecha(Instant fecha) { this.fecha = fecha; }
  public String getCliente() { return cliente; }
  public void setCliente(String cliente) { this.cliente = cliente; }
  public String getRfc() { return rfc; }
  public void setRfc(String rfc) { this.rfc = rfc; }
  public String getRazonSocial() { return razonSocial; }
  public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
  public String getNoTicket() { return noTicket; }
  public void setNoTicket(String noTicket) { this.noTicket = noTicket; }
  public BigDecimal getSubtotal() { return subtotal; }
  public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
  public BigDecimal getImpuesto() { return impuesto; }
  public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
  public BigDecimal getIva() { return iva; }
  public void setIva(BigDecimal iva) { this.iva = iva; }
  public BigDecimal getIsr() { return isr; }
  public void setIsr(BigDecimal isr) { this.isr = isr; }
  public BigDecimal getCedular() { return cedular; }
  public void setCedular(BigDecimal cedular) { this.cedular = cedular; }
  public String getEstatus() { return estatus; }
  public void setEstatus(String estatus) { this.estatus = estatus; }
  public String getUuid() { return uuid; }
  public void setUuid(String uuid) { this.uuid = uuid; }
  public String getFacturapiId() { return facturapiId; }
  public void setFacturapiId(String facturapiId) { this.facturapiId = facturapiId; }
  public String getCancellationStatus() { return cancellationStatus; }
  public void setCancellationStatus(String cancellationStatus) { this.cancellationStatus = cancellationStatus; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public List<TicketFactura> getTickets() { return tickets; }
  public void setTickets(List<TicketFactura> tickets) { this.tickets = tickets == null ? List.of() : List.copyOf(tickets); }
}
