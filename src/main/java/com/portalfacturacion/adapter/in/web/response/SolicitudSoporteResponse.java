package com.portalfacturacion.adapter.in.web.response;

public record SolicitudSoporteResponse(String mensaje, String destinatario) {

  public static SolicitudSoporteResponse of(String destinatario) {
    return new SolicitudSoporteResponse("Solicitud registrada correctamente", destinatario);
  }
}
