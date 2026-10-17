package com.portalfacturacion.domain.model;

import java.math.BigDecimal;

public class FacturaTimbrada {

  private String id;
  private String uuid;
  private String serie;
  private String folio;
  private String status;
  private String cancellationStatus;
  private BigDecimal subtotal;
  private BigDecimal impuesto;
  private BigDecimal total;
  private String verificationUrl;

  public FacturaTimbrada() {
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getUuid() { return uuid; }
  public void setUuid(String uuid) { this.uuid = uuid; }
  public String getSerie() { return serie; }
  public void setSerie(String serie) { this.serie = serie; }
  public String getFolio() { return folio; }
  public void setFolio(String folio) { this.folio = folio; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getCancellationStatus() { return cancellationStatus; }
  public void setCancellationStatus(String cancellationStatus) { this.cancellationStatus = cancellationStatus; }
  public BigDecimal getSubtotal() { return subtotal; }
  public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
  public BigDecimal getImpuesto() { return impuesto; }
  public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
  public String getVerificationUrl() { return verificationUrl; }
  public void setVerificationUrl(String verificationUrl) { this.verificationUrl = verificationUrl; }
}
