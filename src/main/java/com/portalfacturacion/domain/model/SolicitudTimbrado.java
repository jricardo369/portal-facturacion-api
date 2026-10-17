package com.portalfacturacion.domain.model;

import java.math.BigDecimal;
import java.util.List;

public class SolicitudTimbrado {

  private String rfc;
  private String razonSocial;
  private String regimenFiscal;
  private String codigoPostal;
  private String correo;
  private String usoCfdi;
  private String formaPago;
  private String metodoPago;
  private boolean precioConImpuestos;
  private String folioExterno;
  private List<ItemSolicitudTimbrado> items;
  private boolean publicoGeneral;
  private String periodoPeriodicity;
  private String periodoMeses;
  private Integer periodoAnio;

  public SolicitudTimbrado() {
  }

  public String getRfc() { return rfc; }
  public void setRfc(String rfc) { this.rfc = rfc; }
  public String getRazonSocial() { return razonSocial; }
  public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
  public String getRegimenFiscal() { return regimenFiscal; }
  public void setRegimenFiscal(String regimenFiscal) { this.regimenFiscal = regimenFiscal; }
  public String getCodigoPostal() { return codigoPostal; }
  public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
  public String getCorreo() { return correo; }
  public void setCorreo(String correo) { this.correo = correo; }
  public String getUsoCfdi() { return usoCfdi; }
  public void setUsoCfdi(String usoCfdi) { this.usoCfdi = usoCfdi; }
  public String getFormaPago() { return formaPago; }
  public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
  public String getMetodoPago() { return metodoPago; }
  public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
  public boolean isPrecioConImpuestos() { return precioConImpuestos; }
  public void setPrecioConImpuestos(boolean precioConImpuestos) { this.precioConImpuestos = precioConImpuestos; }
  public String getFolioExterno() { return folioExterno; }
  public void setFolioExterno(String folioExterno) { this.folioExterno = folioExterno; }
  public List<ItemSolicitudTimbrado> getItems() { return items; }
  public void setItems(List<ItemSolicitudTimbrado> items) { this.items = items; }
  public boolean isPublicoGeneral() { return publicoGeneral; }
  public void setPublicoGeneral(boolean publicoGeneral) { this.publicoGeneral = publicoGeneral; }
  public String getPeriodoPeriodicity() { return periodoPeriodicity; }
  public void setPeriodoPeriodicity(String periodoPeriodicity) { this.periodoPeriodicity = periodoPeriodicity; }
  public String getPeriodoMeses() { return periodoMeses; }
  public void setPeriodoMeses(String periodoMeses) { this.periodoMeses = periodoMeses; }
  public Integer getPeriodoAnio() { return periodoAnio; }
  public void setPeriodoAnio(Integer periodoAnio) { this.periodoAnio = periodoAnio; }
}
