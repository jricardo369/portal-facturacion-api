package com.portalfacturacion.domain.model;

import java.math.BigDecimal;

public class ItemSolicitudTimbrado {

  private BigDecimal cantidad;
  private String descripcion;
  private BigDecimal precio;
  private String clave;
  private BigDecimal iva;
  private BigDecimal isr;

  public ItemSolicitudTimbrado() {
  }

  public ItemSolicitudTimbrado(BigDecimal cantidad, String descripcion, BigDecimal precio, String clave) {
    this.cantidad = cantidad;
    this.descripcion = descripcion;
    this.precio = precio;
    this.clave = clave;
  }

  public BigDecimal getCantidad() { return cantidad; }
  public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public BigDecimal getPrecio() { return precio; }
  public void setPrecio(BigDecimal precio) { this.precio = precio; }
  public String getClave() { return clave; }
  public void setClave(String clave) { this.clave = clave; }
  public BigDecimal getIva() { return iva; }
  public void setIva(BigDecimal iva) { this.iva = iva; }
  public BigDecimal getIsr() { return isr; }
  public void setIsr(BigDecimal isr) { this.isr = isr; }
}
