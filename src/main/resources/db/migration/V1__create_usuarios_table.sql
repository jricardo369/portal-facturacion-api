CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario VARCHAR(64) NOT NULL,
  password VARCHAR(255) NOT NULL,
  nombre VARCHAR(150) NULL,
  direccion VARCHAR(255) NULL,
  telefono VARCHAR(32) NULL,
  tipo VARCHAR(32) NULL DEFAULT 'OPERADOR',
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_usuarios_usuario UNIQUE (usuario),
  CONSTRAINT chk_usuarios_tipo CHECK (tipo IN ('ADMIN','OPERADOR','CONSULTA'))
);

CREATE INDEX idx_usuarios_usuario ON usuarios (usuario);
CREATE INDEX idx_usuarios_tipo ON usuarios (tipo);
CREATE INDEX idx_usuarios_activo ON usuarios (activo);
