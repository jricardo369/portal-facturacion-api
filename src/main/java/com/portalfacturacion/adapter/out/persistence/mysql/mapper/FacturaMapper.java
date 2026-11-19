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
    Factura f = new Factura(e.getIdFactura(), e.getFolio(), e.getSerie(), e.getFecha(),
        e.getCliente(), e.getRfc(), e.getRazonSocial(),
        e.getSubtotal(), e.getImpuesto(), e.getTotal(),
        e.getEstatus(), e.getUuid(), e.getFacturapiId(), e.getCancellationStatus(),
        e.getCreatedAt(), e.getUpdatedAt());
    f.setIva(e.getIva());
    f.setIsr(e.getIsr());
    f.setCedular(e.getCedular());
    return f;
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
    e.setSubtotal(d.getSubtotal() == null ? java.math.BigDecimal.ZERO : d.getSubtotal());
    e.setImpuesto(d.getImpuesto() == null ? java.math.BigDecimal.ZERO : d.getImpuesto());
    e.setIva(d.getIva() == null ? java.math.BigDecimal.ZERO : d.getIva());
    e.setIsr(d.getIsr() == null ? java.math.BigDecimal.ZERO : d.getIsr());
    e.setCedular(d.getCedular() == null ? java.math.BigDecimal.ZERO : d.getCedular());
    e.setTotal(d.getTotal() == null ? java.math.BigDecimal.ZERO : d.getTotal());
    e.setEstatus(d.getEstatus());
    e.setUuid(d.getUuid());
    e.setFacturapiId(d.getFacturapiId());
    e.setCancellationStatus(d.getCancellationStatus());
    e.setCreatedAt(d.getCreatedAt());
    e.setUpdatedAt(d.getUpdatedAt());
    return e;
  }
}
