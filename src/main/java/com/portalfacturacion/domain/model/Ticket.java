package com.portalfacturacion.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Ticket {
  private String numeroTicket;
  private BigDecimal total;
  private BigDecimal subtotal;
  private BigDecimal impuestos;
  private String estado;
  private String tipoVenta;
  private Instant fechaEmision;
  private Instant fechaCierre;
  private String cliente;
  private String mesa;
  private String sucursal;
  private boolean facturable;
  private List<TicketItem> items;
  private List<TicketPago> pagos;

  public Ticket() {
  }

  public Ticket(String numeroTicket, BigDecimal total, BigDecimal subtotal, BigDecimal impuestos,
                String estado, String tipoVenta, Instant fechaEmision, Instant fechaCierre,
                String cliente, String mesa, String sucursal, boolean facturable,
                List<TicketItem> items, List<TicketPago> pagos) {
    this.numeroTicket = numeroTicket;
    this.total = total;
    this.subtotal = subtotal;
    this.impuestos = impuestos;
    this.estado = estado;
    this.tipoVenta = tipoVenta;
    this.fechaEmision = fechaEmision;
    this.fechaCierre = fechaCierre;
    this.cliente = cliente;
    this.mesa = mesa;
    this.sucursal = sucursal;
    this.facturable = facturable;
    this.items = items == null ? List.of() : List.copyOf(items);
    this.pagos = pagos == null ? List.of() : List.copyOf(pagos);
  }

  public String getNumeroTicket() { return numeroTicket; }
  public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
  public BigDecimal getSubtotal() { return subtotal; }
  public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
  public BigDecimal getImpuestos() { return impuestos; }
  public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }
  public String getEstado() { return estado; }
  public void setEstado(String estado) { this.estado = estado; }
  public String getTipoVenta() { return tipoVenta; }
  public void setTipoVenta(String tipoVenta) { this.tipoVenta = tipoVenta; }
  public Instant getFechaEmision() { return fechaEmision; }
  public void setFechaEmision(Instant fechaEmision) { this.fechaEmision = fechaEmision; }
  public Instant getFechaCierre() { return fechaCierre; }
  public void setFechaCierre(Instant fechaCierre) { this.fechaCierre = fechaCierre; }
  public String getCliente() { return cliente; }
  public void setCliente(String cliente) { this.cliente = cliente; }
  public String getMesa() { return mesa; }
  public void setMesa(String mesa) { this.mesa = mesa; }
  public String getSucursal() { return sucursal; }
  public void setSucursal(String sucursal) { this.sucursal = sucursal; }
  public boolean isFacturable() { return facturable; }
  public void setFacturable(boolean facturable) { this.facturable = facturable; }
  public List<TicketItem> getItems() { return items; }
  public void setItems(List<TicketItem> items) { this.items = items; }
  public List<TicketPago> getPagos() { return pagos; }
  public void setPagos(List<TicketPago> pagos) { this.pagos = pagos; }
}
