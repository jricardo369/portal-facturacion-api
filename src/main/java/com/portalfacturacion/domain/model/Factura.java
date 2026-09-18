package com.portalfacturacion.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

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
  private BigDecimal total;
  private String estatus;
  private String uuid;
  private Instant createdAt;
  private Instant updatedAt;

  public Factura() {
  }

  public Factura(Long idFactura, String folio, String serie, Instant fecha,
                 String cliente, String rfc, String razonSocial, String noTicket,
                 BigDecimal subtotal, BigDecimal impuesto, BigDecimal total,
                 String estatus, String uuid, Instant createdAt, Instant updatedAt) {
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
  public String getEstatus() { return estatus; }
  public void setEstatus(String estatus) { this.estatus = estatus; }
  public String getUuid() { return uuid; }
  public void setUuid(String uuid) { this.uuid = uuid; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
