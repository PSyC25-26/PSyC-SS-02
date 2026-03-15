USE banca_online;

CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) UNIQUE,
    nombre VARCHAR(100),
    primer_apellido VARCHAR(100),
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE,
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cuenta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    tipo ENUM('CORRIENTE','AHORRO') NOT NULL,
    saldo DECIMAL(10,2) DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

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


