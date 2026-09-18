package com.portalfacturacion.adapter.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "clientes")
public class ClienteEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_cliente")
  private Long idCliente;

  @Column(name = "rfc", nullable = false, unique = true, length = 13)
  private String rfc;

  @Column(name = "razon_social", nullable = false, length = 255)
  private String razonSocial;

  @Column(name = "calle", length = 150)
  private String calle;

  @Column(name = "num_exterior", length = 20)
  private String numExterior;

  @Column(name = "num_interior", length = 20)
  private String numInterior;

  @Column(name = "referencia", length = 255)
  private String referencia;

  @Column(name = "estado", length = 100)
  private String estado;

  @Column(name = "municipio", length = 100)
  private String municipio;

  @Column(name = "colonia", length = 100)
  private String colonia;

  @Column(name = "codigo_postal", length = 10)
  private String codigoPostal;

  @Column(name = "correo_electronico", length = 150)
  private String correoElectronico;

  @Column(name = "regimen_fiscal", length = 10)
  private String regimenFiscal;

  @Column(name = "uso_factura", length = 10)
  private String usoFactura;

  @Column(name = "estatus", nullable = false)
  private Boolean estatus = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

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
