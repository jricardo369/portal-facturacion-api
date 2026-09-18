package com.portalfacturacion.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlTestcontainersIT {

  static boolean dockerDisponible() {
    try {
      return org.testcontainers.DockerClientFactory.instance().isDockerAvailable();
    } catch (Exception ex) {
      return false;
    }
  }

  @Test
  void mysql_levanta_y_aceptaConexiones() {
    Assumptions.assumeTrue(dockerDisponible(), "Docker no disponible; se omite Testcontainers");
    try (MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("facturacion_test")
        .withUsername("test")
        .withPassword("test")) {
      mysql.start();
      assertTrue(mysql.isRunning());
    }
  }
}
