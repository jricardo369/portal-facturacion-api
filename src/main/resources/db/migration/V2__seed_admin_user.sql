-- Seed admin inicial (bootstrap). Password en claro: Admin123! -> cambiar tras primer login.
INSERT INTO usuarios (usuario, password, nombre, direccion, telefono, tipo, activo, created_at, updated_at)
SELECT 'admin', '$2b$10$xyphRvs17p.a6V4i30AwducELNR7iPvDRDHkFlzpS2p4J6U9/1RUi', 'Administrador', NULL, NULL, 'ADMIN', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE usuario = 'admin');
