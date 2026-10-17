package com.portalfacturacion.domain.exception;

public class TimbradoFallidoException extends RuntimeException {

  public TimbradoFallidoException(String message) {
    super(message);
  }

  public TimbradoFallidoException(String message, Throwable cause) {
    super(message, cause);
  }
}
