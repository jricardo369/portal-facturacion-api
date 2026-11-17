CREATE TABLE IF NOT EXISTS tickets_factura (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_factura BIGINT NOT NULL,
  numero_ticket VARCHAR(50) NOT NULL,
  fecha_emision TIMESTAMP NULL,
  hora VARCHAR(20) NULL,
  subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  iva DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  isr DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  cedular DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tickets_factura_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON DELETE CASCADE,
  INDEX idx_tickets_factura_factura (id_factura),
  INDEX idx_tickets_factura_ticket (numero_ticket)
);
