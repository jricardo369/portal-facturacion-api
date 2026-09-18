package com.portalfacturacion.domain.model;

import java.math.BigDecimal;

public class TicketItem {
  private String id;
  private String descripcion;
  private BigDecimal cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal importe;

  public TicketItem() {
  }

  public TicketItem(String id, String descripcion, BigDecimal cantidad,
                    BigDecimal precioUnitario, BigDecimal importe) {
    this.id = id;
    this.descripcion = descripcion;
    this.cantidad = cantidad;
    this.precioUnitario = precioUnitario;
    this.importe = importe;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public BigDecimal getCantidad() { return cantidad; }
  public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
  public BigDecimal getPrecioUnitario() { return precioUnitario; }
  public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
  public BigDecimal getImporte() { return importe; }
  public void setImporte(BigDecimal importe) { this.importe = importe; }
}
