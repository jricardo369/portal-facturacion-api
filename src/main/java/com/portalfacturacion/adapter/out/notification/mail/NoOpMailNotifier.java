package com.portalfacturacion.adapter.out.notification.mail;

import com.portalfacturacion.application.port.out.notification.NotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpMailNotifier implements NotificacionPort {

  private static final Logger log = LoggerFactory.getLogger(NoOpMailNotifier.class);

  @Override
  public void notificar(String destinatario, String asunto, String mensaje) {
    log.info("Notificacion omitida (sin proveedor). destinatario={} asunto={}", destinatario, asunto);
  }
}
