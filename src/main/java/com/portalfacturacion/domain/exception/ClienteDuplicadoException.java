package com.portalfacturacion.domain.exception;

public class ClienteDuplicadoException extends RuntimeException {
  public ClienteDuplicadoException(String message) {
    super(message);
  }
}
