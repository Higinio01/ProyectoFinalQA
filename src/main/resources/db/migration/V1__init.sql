-- Tabla de roles
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       rol_nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla de usuarios
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(255) NOT NULL,
                          apellido VARCHAR(255) NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          id_rol BIGINT NOT NULL,
                          CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES roles(id)
);

-- Tabla de productos
CREATE TABLE productos (
                           id BIGSERIAL PRIMARY KEY,
                           nombre VARCHAR(255) NOT NULL,
                           descripcion TEXT,
                           categoria VARCHAR(50) NOT NULL,
                           precio FLOAT NOT NULL CHECK (precio >= 0),
                           stock INT NOT NULL CHECK (stock >= 0)
);

-- Tabla de movimientos de inventario
CREATE TABLE movimiento_inventario (
                                       id BIGSERIAL PRIMARY KEY,
                                       producto_id BIGINT NOT NULL,
                                       cantidad INT,
                                       tipo_movimiento VARCHAR(50) CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA')),
                                       fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       CONSTRAINT fk_movimiento_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);