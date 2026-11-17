package com.portalfacturacion.application.port.in.contacto;

public record SolicitudSoporteCommand(
    String nombre,
    Motivo motivo,
    String fecha,
    String hora,
    String total,
    String descripcion) {

  public enum Motivo {
    DUDA, ACLARACION;

    public static Motivo desde(String valor) {
      if (valor == null) {
        throw new IllegalArgumentException("El motivo es requerido");
      }
      String normalizado = valor.trim().toUpperCase();
      if (normalizado.equals("ACLARACIÓN") || normalizado.equals("ACLARACION")
          || normalizado.equals("ACLARACION FACTURA") || normalizado.equals("ACLARACIÓN FACTURA")) {
        return ACLARACION;
      }
      try {
        return Motivo.valueOf(normalizado);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Motivo no soportado: " + valor + ". Valores permitidos: DUDA, ACLARACION");
      }
    }
  }
}
