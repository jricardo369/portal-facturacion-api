package com.portalfacturacion.adapter.out.file;

import org.springframework.stereotype.Component;

@Component
public class LocalFileStorageAdapter {

  public String guardar(byte[] contenido, String nombre) {
    throw new UnsupportedOperationException("Almacenamiento de archivos no implementado aun");
  }
}
