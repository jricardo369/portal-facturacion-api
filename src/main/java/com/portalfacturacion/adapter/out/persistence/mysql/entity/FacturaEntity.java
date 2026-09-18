package com.portalfacturacion.adapter.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "factura")
public class FacturaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_factura")
  private Long idFactura;

  @Column(name = "folio", nullable = false, length = 30)
  private String folio;

  @Column(name = "serie", length = 10)
  private String serie;

  @Column(name = "fecha", nullable = false)
  private Instant fecha;

  @Column(name = "cliente", length = 255)
  private String cliente;

  @Column(name = "rfc", nullable = false, length = 13)
  private String rfc;

  @Column(name = "razon_social", nullable = false, length = 255)
  private String razonSocial;

  @Column(name = "no_ticket", nullable = false, length = 30)
  private String noTicket;

  @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "impuesto", nullable = false, precision = 12, scale = 2)
  private BigDecimal impuesto;

  @Column(name = "total", nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Column(name = "estatus", nullable = false, length = 20)
  private String estatus = "VIGENTE";

  @Column(name = "uuid", length = 36, unique = true)
  private String uuid;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

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
