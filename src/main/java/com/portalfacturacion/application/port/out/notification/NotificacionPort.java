package com.portalfacturacion.application.port.out.notification;

public interface NotificacionPort {
  void notificar(String destinatario, String asunto, String mensaje);
}
