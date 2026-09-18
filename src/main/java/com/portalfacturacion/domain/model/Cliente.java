package com.portalfacturacion.domain.model;

import java.time.Instant;

public class Cliente {
  private Long idCliente;
  private String rfc;
  private String razonSocial;
  private String calle;
  private String numExterior;
  private String numInterior;
  private String referencia;
  private String estado;
  private String municipio;
  private String colonia;
  private String codigoPostal;
  private String correoElectronico;
  private String regimenFiscal;
  private String usoFactura;
  private Boolean estatus;
  private Instant createdAt;
  private Instant updatedAt;

  public Cliente() {
  }

  public Cliente(Long idCliente, String rfc, String razonSocial, String calle,
                 String numExterior, String numInterior, String referencia,
                 String estado, String municipio, String colonia, String codigoPostal,
                 String correoElectronico, String regimenFiscal, String usoFactura,
                 Boolean estatus, Instant createdAt, Instant updatedAt) {
    this.idCliente = idCliente;
    this.rfc = rfc;
    this.razonSocial = razonSocial;
    this.calle = calle;
    this.numExterior = numExterior;
    this.numInterior = numInterior;
    this.referencia = referencia;
    this.estado = estado;
    this.municipio = municipio;
    this.colonia = colonia;
    this.codigoPostal = codigoPostal;
    this.correoElectronico = correoElectronico;
    this.regimenFiscal = regimenFiscal;
    this.usoFactura = usoFactura;
    this.estatus = estatus;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getIdCliente() { return idCliente; }
  public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
  public String getRfc() { return rfc; }
  public void setRfc(String rfc) { this.rfc = rfc; }
  public String getRazonSocial() { return razonSocial; }
  public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
  public String getCalle() { return calle; }
  public void setCalle(String calle) { this.calle = calle; }
  public String getNumExterior() { return numExterior; }
  public void setNumExterior(String numExterior) { this.numExterior = numExterior; }
  public String getNumInterior() { return numInterior; }
  public void setNumInterior(String numInterior) { this.numInterior = numInterior; }
  public String getReferencia() { return referencia; }
  public void setReferencia(String referencia) { this.referencia = referencia; }
  public String getEstado() { return estado; }
  public void setEstado(String estado) { this.estado = estado; }
  public String getMunicipio() { return municipio; }
  public void setMunicipio(String municipio) { this.municipio = municipio; }
  public String getColonia() { return colonia; }
  public void setColonia(String colonia) { this.colonia = colonia; }
  public String getCodigoPostal() { return codigoPostal; }
  public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
  public String getCorreoElectronico() { return correoElectronico; }
  public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
  public String getRegimenFiscal() { return regimenFiscal; }
  public void setRegimenFiscal(String regimenFiscal) { this.regimenFiscal = regimenFiscal; }
  public String getUsoFactura() { return usoFactura; }
  public void setUsoFactura(String usoFactura) { this.usoFactura = usoFactura; }
  public Boolean getEstatus() { return estatus; }
  public void setEstatus(Boolean estatus) { this.estatus = estatus; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
