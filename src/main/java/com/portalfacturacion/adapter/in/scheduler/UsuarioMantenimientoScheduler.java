package com.portalfacturacion.adapter.in.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMantenimientoScheduler {

  private static final Logger log = LoggerFactory.getLogger(UsuarioMantenimientoScheduler.class);

  @Scheduled(cron = "0 0 3 * * *")
  public void depurar() {
    log.debug("Job de mantenimiento de usuarios ejecutado");
  }
}
