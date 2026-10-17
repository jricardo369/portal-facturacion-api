package com.portalfacturacion.domain.exception;

public class FacturapiNoDisponibleException extends RuntimeException {

  public FacturapiNoDisponibleException(String message) {
    super(message);
  }

  public FacturapiNoDisponibleException(String message, Throwable cause) {
    super(message, cause);
  }
}
