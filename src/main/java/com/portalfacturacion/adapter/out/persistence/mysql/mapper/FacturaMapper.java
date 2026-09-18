package com.portalfacturacion.adapter.out.persistence.mysql.mapper;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.FacturaEntity;
import com.portalfacturacion.domain.model.Factura;

public final class FacturaMapper {

  private FacturaMapper() {
  }

  public static Factura toDomain(FacturaEntity e) {
    if (e == null) {
      return null;
    }
    return new Factura(e.getIdFactura(), e.getFolio(), e.getSerie(), e.getFecha(),
        e.getCliente(), e.getRfc(), e.getRazonSocial(), e.getNoTicket(),
        e.getSubtotal(), e.getImpuesto(), e.getTotal(),
        e.getEstatus(), e.getUuid(), e.getCreatedAt(), e.getUpdatedAt());
  }

  public static FacturaEntity toEntity(Factura d) {
    FacturaEntity e = new FacturaEntity();
    e.setIdFactura(d.getIdFactura());
    e.setFolio(d.getFolio());
    e.setSerie(d.getSerie());
    e.setFecha(d.getFecha());
    e.setCliente(d.getCliente());
    e.setRfc(d.getRfc());
    e.setRazonSocial(d.getRazonSocial());
    e.setNoTicket(d.getNoTicket());
    e.setSubtotal(d.getSubtotal());
    e.setImpuesto(d.getImpuesto());
    e.setTotal(d.getTotal());
    e.setEstatus(d.getEstatus());
    e.setUuid(d.getUuid());
    e.setCreatedAt(d.getCreatedAt());
    e.setUpdatedAt(d.getUpdatedAt());
    return e;
  }
}
