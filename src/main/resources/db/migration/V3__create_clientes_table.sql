CREATE TABLE IF NOT EXISTS clientes (
  id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
  rfc VARCHAR(13) NOT NULL,
  razon_social VARCHAR(255) NOT NULL,
  calle VARCHAR(150) NULL,
  num_exterior VARCHAR(20) NULL,
  num_interior VARCHAR(20) NULL,
  referencia VARCHAR(255) NULL,
  estado VARCHAR(100) NULL,
  municipio VARCHAR(100) NULL COMMENT 'Municipio o alcaldia/delegacion',
  colonia VARCHAR(100) NULL,
  codigo_postal VARCHAR(10) NULL,
  correo_electronico VARCHAR(150) NULL,
  regimen_fiscal VARCHAR(10) NULL COMMENT 'Catalogo SAT c_RegimenFiscal, ej. 601, 605, 606',
  uso_factura VARCHAR(10) NULL COMMENT 'Catalogo SAT c_UsoCFDI, ej. G01, G03, P01',
  estatus BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_clientes_rfc UNIQUE (rfc)
);

CREATE INDEX idx_clientes_rfc ON clientes (rfc);
CREATE INDEX idx_clientes_razon_social ON clientes (razon_social);
CREATE INDEX idx_clientes_codigo_postal ON clientes (codigo_postal);
CREATE INDEX idx_clientes_estatus ON clientes (estatus);
