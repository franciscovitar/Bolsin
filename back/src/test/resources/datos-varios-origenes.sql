-- Escenario aislado para probar el CU 29 con más de una comisión médica de origen.
-- NO forma parte de los datos de la demo: se carga después de data.sql sólo en la prueba que lo pide.
-- Al bolsín 101 (precinto 5001, Central -> Córdoba) se le agrega el remito 1009, generado por la
-- Comisión Médica Rosario, con la documentación 13.

INSERT INTO remito (numero, fecha, estado_id, origen_codigo, destino_codigo, bolsin_numero) VALUES
  (1009, '2026-11-04', 3, 3, 2, 101);

INSERT INTO documentacion (numero, asunto, fecha_pase, tipo_documento_id, estado_actual) VALUES
  (13, 'Dictamen - Ferrer, Inés', '2026-11-03', 2, 'EnBolsinEnviado');

INSERT INTO detalle_remito (id, area_cmc_destino, documentacion_numero, remito_numero) VALUES
  (13, NULL, 13, 1009);

INSERT INTO cambio_estado_documentacion (fecha_hora_inicio, fecha_hora_fin, estado, responsable_id, documentacion_numero) VALUES
  ('2026-11-03 09:00:00', '2026-11-04 10:00:00', 'Registrada', 2, 13),
  ('2026-11-04 10:00:00', '2026-11-05 11:00:00', 'EnRemito', 2, 13),
  ('2026-11-05 11:00:00', '2026-11-06 08:30:00', 'EnBolsinSaliente', 2, 13),
  ('2026-11-06 08:30:00', NULL, 'EnBolsinEnviado', 2, 13);
