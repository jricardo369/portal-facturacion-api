CREATE TABLE IF NOT EXISTS configuracion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  clave VARCHAR(64) NOT NULL,
  valor VARCHAR(255) NOT NULL,
  descripcion VARCHAR(255),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_configuracion_clave UNIQUE (clave)
);

INSERT INTO configuracion (clave, valor, descripcion)
VALUES ('correo_dudas_aclaraciones', 'osodespiertocoffee@gmail.com', 'Correo destino para notificaciones de dudas y aclaraciones de facturas')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);
