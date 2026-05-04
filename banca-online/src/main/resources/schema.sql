USE banca_online;

#     drop table if exists transaccion;
#     drop table if exists cuenta;
#     drop table if exists cliente;
#     drop table if exists usuario;

  CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN','CLIENTE') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id BIGINT NULL
  );

CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    primer_apellido VARCHAR(100),
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    email VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cuenta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    numero_cuenta VARCHAR(30) UNIQUE NOT NULL,
    tipo_cuenta ENUM('CORRIENTE','AHORRO') NOT NULL,
    saldo DOUBLE NOT NULL DEFAULT 0,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

-- Migración ligera: bases de datos creadas antes de la columna `activa`
-- mantienen la tabla `cuenta` sin actualizarse (`CREATE IF NOT EXISTS` no altera el esquema).
-- Nota: en MySQL 5.7 no existe `ADD COLUMN IF NOT EXISTS`; el proyecto usa
-- `spring.sql.init.continue-on-error=true` para tolerar el error si la columna ya existe.
ALTER TABLE cuenta ADD COLUMN activa BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS transaccion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('DEPOSITO','RETIRO','TRANSFERENCIA') NOT NULL,
    descripcion VARCHAR(200),
    total DECIMAL(10,2) NOT NULL,
    cuenta_origen_id BIGINT,
    cuenta_destino_id BIGINT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cuenta_origen_id) REFERENCES cuenta(id),
    FOREIGN KEY (cuenta_destino_id) REFERENCES cuenta(id)
);

