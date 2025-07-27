-- Nota: La contraseña hasheada es para "password123"
INSERT INTO usuarios (nombre, apellido, email, password, id_rol, estado) VALUES
    ('Admin', 'Sistema', 'admin@example.com', '$2a$12$v4IPBAuKUW4sKPAWNLJHEevamRtpDKk3.cqCM0jBckCagTRsnG7QO',
    (SELECT id FROM roles WHERE rol_nombre = 'ADMIN'),'ACTIVO'),
    ('Juan', 'Pérez', 'empleado@example.com', '$2a$12$v4IPBAuKUW4sKPAWNLJHEevamRtpDKk3.cqCM0jBckCagTRsnG7QO',
    (SELECT id FROM roles WHERE rol_nombre = 'EMPLEADO'),'ACTIVO'),
    ('María', 'García', 'cliente@example.com', '$2a$12$v4IPBAuKUW4sKPAWNLJHEevamRtpDKk3.cqCM0jBckCagTRsnG7QO',
    (SELECT id FROM roles WHERE rol_nombre = 'CLIENTE'),'ACTIVO');

-- Insertar productos de diferentes categorías
INSERT INTO productos (nombre, descripcion, categoria, precio, stock, minimo_stock) VALUES
    -- Electrónica
    ('Laptop HP Pavilion', 'Laptop con procesador Intel Core i5, 8GB RAM, 256GB SSD', 'ELECTRONICA', 899.99, 15, 5),
    ('Smartphone Samsung Galaxy', 'Teléfono inteligente con pantalla de 6.5 pulgadas', 'ELECTRONICA', 599.99, 25, 5),
    ('Auriculares Bluetooth Sony', 'Auriculares inalámbricos con cancelación de ruido', 'ELECTRONICA', 149.99, 30, 5),

    -- Ropa
    ('Camisa Polo Ralph Lauren', 'Camisa polo de algodón, color azul marino', 'ROPA', 79.99, 50, 5),
    ('Jeans Levi''s 501', 'Pantalón de mezclilla clásico', 'ROPA', 89.99, 40, 5),
    ('Zapatos Nike Air Max', 'Zapatillas deportivas para correr', 'ROPA', 129.99, 35, 5),

    -- Alimentos
    ('Aceite de Oliva Extra Virgen', 'Aceite de oliva italiano, 1 litro', 'ALIMENTOS', 12.99, 100, 5),
    ('Pasta Barilla Spaghetti', 'Pasta italiana tradicional, 500g', 'ALIMENTOS', 3.99, 200, 5),
    ('Chocolate Lindt 70% Cacao', 'Chocolate negro premium, 100g', 'ALIMENTOS', 4.99, 150, 5);