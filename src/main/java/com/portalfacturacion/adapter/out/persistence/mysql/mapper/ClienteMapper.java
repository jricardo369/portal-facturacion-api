package com.portalfacturacion.adapter.out.persistence.mysql.mapper;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.ClienteEntity;
import com.portalfacturacion.domain.model.Cliente;

public final class ClienteMapper {

  private ClienteMapper() {
  }

  public static Cliente toDomain(ClienteEntity e) {
    if (e == null) {
      return null;
    }
    return new Cliente(e.getIdCliente(), e.getRfc(), e.getRazonSocial(), e.getCalle(),
        e.getNumExterior(), e.getNumInterior(), e.getReferencia(), e.getEstado(),
        e.getMunicipio(), e.getColonia(), e.getCodigoPostal(), e.getCorreoElectronico(),
        e.getRegimenFiscal(), e.getUsoFactura(), e.getEstatus(),
        e.getCreatedAt(), e.getUpdatedAt());
  }

  public static ClienteEntity toEntity(Cliente d) {
    ClienteEntity e = new ClienteEntity();
    e.setIdCliente(d.getIdCliente());
    e.setRfc(d.getRfc());
    e.setRazonSocial(d.getRazonSocial());
    e.setCalle(d.getCalle());
    e.setNumExterior(d.getNumExterior());
    e.setNumInterior(d.getNumInterior());
    e.setReferencia(d.getReferencia());
    e.setEstado(d.getEstado());
    e.setMunicipio(d.getMunicipio());
    e.setColonia(d.getColonia());
    e.setCodigoPostal(d.getCodigoPostal());
    e.setCorreoElectronico(d.getCorreoElectronico());
    e.setRegimenFiscal(d.getRegimenFiscal());
    e.setUsoFactura(d.getUsoFactura());
    e.setEstatus(d.getEstatus() == null ? Boolean.TRUE : d.getEstatus());
    e.setCreatedAt(d.getCreatedAt());
    e.setUpdatedAt(d.getUpdatedAt());
    return e;
  }
}
