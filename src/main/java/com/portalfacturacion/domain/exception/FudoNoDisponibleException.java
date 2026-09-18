package com.portalfacturacion.domain.exception;

public class FudoNoDisponibleException extends RuntimeException {
  public FudoNoDisponibleException(String message) {
    super(message);
  }

  public FudoNoDisponibleException(String message, Throwable cause) {
    super(message, cause);
  }
}
