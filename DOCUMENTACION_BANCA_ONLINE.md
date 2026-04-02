#  Documentación Técnica - Banca Online

**Versión:** 1.0  (Sprint 1)  
**Fecha:** Abril 2026  
**Autores:** Equipo de Desarrollo Banca Online: Eneko Barbadillo, Alberto García, Haizea González, Nora Ibarguren e Imanol Ugarte.  
**Última modificación:** 26 de Marzo de 2026  
**Estado:** En desarrollo

---

##  Tabla de Contenidos

1. [Introducción](#introducción)
2. [Descripción del Proyecto](#descripción-del-proyecto)
3. [Especificaciones Técnicas](#especificaciones-técnicas)
4. [Arquitectura del Sistema](#arquitectura-del-sistema)
5. [Guía de Instalación](#guía-de-instalación)
6. [Configuración](#configuración)
7. [Base de Datos](#base-de-datos)
8. [Seguridad](#seguridad)

---

##  Introducción

Banca Online es una aplicación web diseñada para gestionar operaciones bancarias en línea. El sistema permite a los clientes crear y administrar sus cuentas bancarias, realizar transferencias entre cuentas y consultar su saldo de forma segura.

### Objetivos del Proyecto

- Proporcionar una plataforma segura para la gestión bancaria digital
- Implementar un sistema de autenticación robusto
- Permitir operaciones financieras de manera ágil
- Asegurar la integridad de los datos financieros
- Facilitar la escalabilidad del sistema

---

##  Descripción del Proyecto

### ¿Qué es Banca Online?

Banca Online es una aplicación Backend desarrollada con **Spring Boot** que proporciona una API REST completa para la gestión de:

- **Clientes**: Registro, actualización, búsqueda y eliminación de clientes
- **Cuentas Bancarias**: Creación y gestión de cuentas corrientes y de ahorro
- **Transferencias**: Movimiento de dinero entre cuentas
- **Consultas de Saldo**: Verificación del saldo disponible

### Características Principales

- **API RESTful Completa**: Endpoints para CRUD de clientes y cuentas  
- **Base de Datos MySQL**: Persistencia de datos robusta  
- **Docker Support**: Fácil despliegue con Docker Compose  
- **Tests Unitarios Completos**: Cobertura de pruebas en controllers, services y repositorios  
- **Validación de Datos**: Validación automática con Jakarta Validation  
- **Documentación API**: Swagger integrado  
- **DTOs**: Separación entre entidades y modelos de comunicación  
- **Manejo de Excepciones**: Gestión centralizada de errores  

### Alcance del Proyecto

**Incluido:**
- Gestión completa de clientes
- Gestión de cuentas bancarias
- Sistema de tests
- Consulta de saldos
- Transferencias entre cuentas

**Pasos futuros:**
- Depositar/Retirar dinero
- Consultar cuentas
- Sistema de autenticación (seguridad) e Inicio de Sesión
- Historial de transacciones
- Integración de pantallas
- Tests de cobertura, integración, rendimiento, sistema

---

##  Especificaciones Técnicas

### Stack Tecnológico

| Componente | Tecnología | Versión |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 4.0.3 |
| **Java** | OpenJDK | 25 |
| **Base de Datos** | MySQL | 8.0 |
| **ORM** | Hibernate/JPA | Incluido en Spring |
| **Build Tool** | Maven | 3.9.12 |
| **Testing** | JUnit 5, Mockito | Latest |
| **Documentación API** | Springdoc OpenAPI | 3.0.2 |
| **Validación** | Jakarta Validation | Incluido en Spring |
| **Logging** | SLF4J + Logback | Incluido en Spring |

### Requisitos del Sistema

**Software Requerido:**
- Docker & Docker Compose (Recomendado)
- Java 25+
- Maven 3.9+
- MySQL 8.0+ (o usar Docker)

### Versiones de Dependencias Clave

```xml
<!-- Spring Boot Parent POM -->
4.0.3

<!-- Spring Data JPA -->
Incluido en Spring Boot

<!-- MySQL Driver -->
com.mysql:mysql-connector-j (Runtime scope)

<!-- Lombok -->
1.18.40 (Provided scope)

<!-- Springdoc OpenAPI -->
3.0.2

<!-- Validación -->
jakarta.validation (Incluido en Spring Boot)
```

---

##  Arquitectura del Sistema

### Patrones de Diseño

#### 1. Patrón MVC (Model-View-Controller)
- **Model**: Entidades JPA (Cliente, Cuenta)
- **Controller**: ClienteController, CuentaController
- **View**: API REST (JSON)

#### 2. Patrón Repository
```
Controller → Service → Repository → Database
```

#### 3. Patrón DTO (Data Transfer Object)
- ClienteRequest/ClienteResponse
- CuentaRequest/CuentaResponse
- TransferenciaDTO

### Flujo de Datos

```
Cliente HTTP Request
         ↓
    Spring Filter (Validación)
         ↓
    Controller (Mapeo de rutas)
         ↓
    Service (Lógica de negocio)
         ↓
    Repository (Acceso a datos)
         ↓
    Base de Datos
         ↓
    Response JSON
```

### Capas de la Aplicación

```
┌─────────────────────────────────────────┐
│    Layer de Presentación (Endpoints)    │
│  - ClienteController (/api/clientes)    │
│  - CuentaController (/cuentas)          │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│    Layer de Negocio (Services)          │
│  - ClienteService                       │
│  - CuentaService                        │
│  - TransferService                      │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│    Layer de Datos (Repositories)        │
│  - IClienteRepository                   │
│  - ICuentaRepository                    │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│    Base de Datos (MySQL)                │
│  - Tabla cliente                        │
│  - Tabla cuenta                         │
│  - Tabla transaccion                    │
└─────────────────────────────────────────┘
```

### Relaciones entre Entidades

**Cliente (1) ─────────── (N) Cuenta**

- Un cliente puede tener múltiples cuentas
- Una cuenta pertenece a un cliente
- Relación Many-to-One en JPA

---

##  Guía de Instalación

#### Requisitos Previos
- Docker (https://www.docker.com/get-started)
- Docker Compose (incluido en Docker Desktop)

#### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/banca-online.git
cd banca-online
```

2. **Iniciar los servicios con Docker Compose**
```bash
docker-compose up -d
```

Este comando:
- Descarga la imagen MySQL 8.0
- Crea y inicia el contenedor MySQL
- Expone el puerto 3307 para MySQL
- Crea un volumen para persistencia de datos

3. **Compilar y ejecutar la aplicación**
```bash
./mvnw clean spring-boot:run
```

O con Maven instalado:
```bash
mvn clean spring-boot:run
```

4. **Verificar que la aplicación esté corriendo**
```bash
curl http://localhost:8080/api/clientes
```


5. **Verificar acceso**
- API: http://localhost:8080/api/clientes
- Swagger: http://localhost:8080/swagger-ui.html

---

##  Configuración

### Archivo application.properties

```properties
# Información de la aplicación
spring.application.name=banca-online

# Configuración de la base de datos
spring.datasource.url=jdbc:mysql://localhost:3307/banca_online?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=banca_user
spring.datasource.password=mi_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración de Hibernate/JPA
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always

# Logging
logging.level.org.springframework.jdbc.datasource.init=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Variables de Entorno (Docker)

En `docker-compose.yml`:
```yaml
MYSQL_DATABASE: banca_online
MYSQL_USER: banca_user
MYSQL_PASSWORD: mi_password
MYSQL_ROOT_PASSWORD: root_password
```

### Configuración de Puerto

- **Aplicación**: 8080 (configurable en application.properties)
- **MySQL (Docker)**: 3307 → 3306 (interno)
- **MySQL (Local)**: 3306


---

### Códigos de Respuesta HTTP

| Código | Significado | Ejemplo |
|--------|------------|---------|
| 200 | OK - Solicitud exitosa | GET, PUT |
| 201 | Created - Recurso creado | POST |
| 204 | No Content - Sin respuesta | DELETE |
| 400 | Bad Request - Datos inválidos | Validación fallida |
| 404 | Not Found - Recurso no existe | Cliente/Cuenta no encontrado |
| 500 | Internal Server Error - Error del servidor | Error de BD |

---

##  Base de Datos

### Esquema de Datos

#### Tabla: cliente
```sql
CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    primer_apellido VARCHAR(100),
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

**Índices:**
- PRIMARY KEY: id
- UNIQUE: dni, email
- Para optimizar búsquedas por email

#### Tabla: cuenta
```sql
CREATE TABLE cuenta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    numero_cuenta VARCHAR(30) UNIQUE NOT NULL,
    tipo_cuenta ENUM('CORRIENTE','AHORRO') NOT NULL,
    saldo DOUBLE NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);
```

**Índices:**
- PRIMARY KEY: id
- UNIQUE: numero_cuenta
- FOREIGN KEY: cliente_id

#### Tabla: transaccion
```sql
CREATE TABLE transaccion (
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
```

### Relaciones

```
Cliente (1) ─────────── (N) Cuenta
  |
  └─ PK: id
     FK (en Cuenta): cliente_id

Cuenta (1) ─────────── (N) Transaccion (origen)
Cuenta (1) ─────────── (N) Transaccion (destino)
  |
  └─ PK: id
     FK (en Transaccion): cuenta_origen_id, cuenta_destino_id
```

---

##  Seguridad

### Validaciones Implementadas

1. **DTOs con Validación**
   - `@NotBlank`: Campos no vacíos
   - `@NotNull`: Campos obligatorios
   - `@Email`: Formato de email válido
   - `@Min/@Max`: Rangos numéricos

2. **Restricciones de Base de Datos**
   - UNIQUE en dni y email
   - Foreign keys para referencial integrity
   - NOT NULL en campos obligatorios

3. **Manejo de Excepciones**
   - Controllers validan y devuelven códigos HTTP apropiados
   - Services lanzan excepciones descriptivas
   - Errores de negocio se capturan y manejan





