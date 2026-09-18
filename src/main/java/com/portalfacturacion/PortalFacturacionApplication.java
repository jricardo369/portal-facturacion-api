package com.portalfacturacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan("com.portalfacturacion.configuration")
@EnableScheduling
public class PortalFacturacionApplication {
  public static void main(String[] args) {
    SpringApplication.run(PortalFacturacionApplication.class, args);
  }
}
