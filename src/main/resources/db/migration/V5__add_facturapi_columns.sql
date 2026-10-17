ALTER TABLE factura
  ADD COLUMN facturapi_id VARCHAR(36) NULL,
  ADD COLUMN cancellation_status VARCHAR(20) NULL,
  ADD UNIQUE KEY uq_factura_facturapi_id (facturapi_id);
