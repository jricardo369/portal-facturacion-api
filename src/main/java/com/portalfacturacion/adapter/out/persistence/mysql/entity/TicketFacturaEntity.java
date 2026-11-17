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
@Table(name = "tickets_factura")
public class TicketFacturaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "id_factura", nullable = false)
  private Long idFactura;

  @Column(name = "numero_ticket", nullable = false, length = 50)
  private String numeroTicket;

  @Column(name = "fecha_emision")
  private Instant fechaEmision;

  @Column(name = "hora", length = 20)
  private String hora;

  @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "iva", nullable = false, precision = 12, scale = 2)
  private BigDecimal iva;

  @Column(name = "isr", nullable = false, precision = 12, scale = 2)
  private BigDecimal isr;

  @Column(name = "cedular", nullable = false, precision = 12, scale = 2)
  private BigDecimal cedular;

  @Column(name = "total", nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getIdFactura() { return idFactura; }
  public void setIdFactura(Long idFactura) { this.idFactura = idFactura; }
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
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
