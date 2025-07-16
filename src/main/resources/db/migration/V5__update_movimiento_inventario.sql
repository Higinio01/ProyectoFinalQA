ALTER TABLE movimiento_inventario
    ADD COLUMN cantidad_anterior INT NOT NULL DEFAULT 0,
    ADD COLUMN cantidad_nueva INT NOT NULL DEFAULT 0,
    ADD COLUMN motivo VARCHAR(500),
    ADD COLUMN usuario_responsable VARCHAR(100);

-- Aseguramos longitud consistente en tipo_movimiento
ALTER TABLE movimiento_inventario
ALTER COLUMN tipo_movimiento TYPE VARCHAR(20);

-- Nos aseguramos de que tipo_movimiento siga teniendo el CHECK
ALTER TABLE movimiento_inventario
DROP CONSTRAINT IF EXISTS movimiento_inventario_tipo_movimiento_check,
    ADD CONSTRAINT movimiento_inventario_tipo_movimiento_check CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA'));
