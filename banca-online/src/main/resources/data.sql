-- ============================================================================
-- DATOS SEED - Banca Online
-- ============================================================================
-- Este fichero se ejecuta automaticamente por Spring Boot al arrancar la app.
-- Contiene datos de prueba consistentes para todos.
--
-- Se ejecuta SOLO si la tabla esta vacia (usamos INSERT IGNORE).
-- Si quieres resetear los datos, ejecuta:
--     docker-compose down -v
--     docker-compose up -d
-- ============================================================================

USE banca_online;

-- ============================================================================
-- CLIENTES de prueba
-- ============================================================================
INSERT IGNORE INTO cliente (id, dni, nombre, primer_apellido, segundo_apellido,
                            fecha_nacimiento, email, telefono, direccion, fecha_creacion)
VALUES
    (1, '11111111A', 'Juan', 'Garcia', 'Lopez',
     '1990-05-15', 'cliente@banco.com', '600111111',
     'Calle Mayor 1, Bilbao', CURRENT_TIMESTAMP),

    (2, '22222222B', 'Maria', 'Fernandez', 'Sanchez',
     '1985-09-22', 'maria@banco.com', '600222222',
     'Gran Via 50, Bilbao', CURRENT_TIMESTAMP),

    (3, '33333333C', 'Pedro', 'Martinez', 'Ruiz',
     '1995-03-10', 'pedro@banco.com', '600333333',
     'Avenida Lehendakari 12, Bilbao', CURRENT_TIMESTAMP);

-- ============================================================================
-- USUARIOS (credenciales de acceso)
-- ============================================================================
-- Passwords con BCrypt (generados con BCryptPasswordEncoder del proyecto):
--   admin123    -> hash valido para "admin123"
--   cliente123  -> hash valido para "cliente123" (compartido por los 3 clientes)
-- ============================================================================
INSERT IGNORE INTO usuario (id, email, password, rol, activo, cliente_id)
VALUES
    (1, 'admin@banco.com',
     '$2a$10$ZS.ZuWZ.MfFg4.Su0dXAyeLJB8n3QunWTSth/jXv0AGNnkCoZYr0O',
     'ADMIN', TRUE, NULL),

    (2, 'cliente@banco.com',
     '$2a$10$BjWkiMQ62Ik/NFKDbCRs/el4VKiMIucQzUPUffh48TnxalCBFWQ7e',
     'CLIENTE', TRUE, 1),

    (3, 'maria@banco.com',
     '$2a$10$BjWkiMQ62Ik/NFKDbCRs/el4VKiMIucQzUPUffh48TnxalCBFWQ7e',
     'CLIENTE', TRUE, 2),

    (4, 'pedro@banco.com',
     '$2a$10$BjWkiMQ62Ik/NFKDbCRs/el4VKiMIucQzUPUffh48TnxalCBFWQ7e',
     'CLIENTE', TRUE, 3);

-- ============================================================================
-- CUENTAS BANCARIAS
-- ============================================================================
INSERT IGNORE INTO cuenta (id, cliente_id, numero_cuenta, tipo_cuenta, saldo, activa, fecha_creacion)
VALUES
    (1, 1, 'ES1111000111111111111111', 'CORRIENTE', 1500.00, TRUE, CURRENT_TIMESTAMP),
    (2, 1, 'ES1111000122222222222222', 'AHORRO',    5000.00, TRUE, CURRENT_TIMESTAMP),
    (3, 2, 'ES2222000111111111111111', 'CORRIENTE', 2500.50, TRUE, CURRENT_TIMESTAMP),
    (4, 3, 'ES3333000111111111111111', 'CORRIENTE',  800.00, TRUE, CURRENT_TIMESTAMP);

-- ============================================================================
-- TRANSACCIONES de ejemplo (historial)
-- ============================================================================
INSERT IGNORE INTO transaccion (id, tipo, descripcion, total, cuenta_origen_id, cuenta_destino_id, fecha)
VALUES
    (1, 'DEPOSITO', 'Deposito inicial cuenta corriente',
     1500.00, NULL, 1, CURRENT_TIMESTAMP),

    (2, 'DEPOSITO', 'Deposito inicial cuenta ahorro',
     5000.00, NULL, 2, CURRENT_TIMESTAMP),

    (3, 'TRANSFERENCIA', 'Transferencia entre cuentas propias',
     200.00, 1, 2, CURRENT_TIMESTAMP),

    (4, 'RETIRO', 'Retiro en cajero',
     50.00, 1, NULL, CURRENT_TIMESTAMP);