#  Documentación Técnica - Banca Online

**Versión:** 2.0  (Sprint 2)  
**Fecha:** Abril 2026  
**Autores:** Equipo de Desarrollo Banca Online: Eneko Barbadillo, Alberto García, Haizea González, Nora Ibarguren e Imanol Ugarte.  
**Última modificación:** 20 de Abril de 2026  
**Estado:** Funcional

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
10. [Frontend](#frontend)
11. [Estrategia de Testing](#estrategia-de-testing)

---

##  Introducción

Banca Online es una aplicación web diseñada para gestionar operaciones bancarias en línea. El sistema permite a los usuarios autenticarse con email y contraseña, obtener un token JWT y, según su rol, gestionar clientes y cuentas, realizar operaciones financieras (depósito, retiro, transferencia) y consultar saldos e historial de transacciones de forma segura.

### Objetivos del Proyecto

- Proporcionar una plataforma segura para la gestión bancaria digital
- Implementar un sistema de autenticación JWT robusto con control de acceso por roles
- Permitir operaciones financieras (depósito, retiro, transferencia) de manera ágil
- Asegurar la integridad de los datos financieros mediante transacciones y validaciones
- Registrar un historial completo de todas las operaciones
- Facilitar la escalabilidad y mantenibilidad del sistema

---

##  Descripción del Proyecto

### ¿Qué es Banca Online?

Banca Online es una aplicación Backend desarrollada con **Spring Boot** que proporciona una API REST completa para la gestión de:

- **Usuarios y Autenticación**: Dos roles (`ADMIN`, `CLIENTE`) con JWT stateless
- **Clientes**: Registro, actualización, búsqueda y eliminación de clientes
- **Cuentas Bancarias**: Creación y gestión de cuentas corrientes y de ahorro
- **Operaciones Financieras**: Depósito, retiro y transferencias entre cuentas
- **Historial de Transacciones**: Registro persistente de cada operación en base de datos
- **Consultas de Saldo**: Verificación del saldo disponible con control de acceso

### Características Principales

- **API RESTful Completa**: Endpoints CRUD para todas las operaciones bancarias  
- **Base de Datos MySQL**: Persistencia de datos robusta  
- **Docker Support**: Fácil despliegue con Docker Compose  
- **Tests Unitarios Completos**: Cobertura de pruebas en controllers, services y repositorios  
- **Validación de Datos**: Validación automática con Jakarta Validation  
- **Documentación API**: Swagger integrado  
- **DTOs**: Separación entre entidades y modelos de comunicación  
- **Manejo de Excepciones**: Gestión centralizada de errores 
- **Autenticación JWT**: Tokens firmados con HMAC-SHA256, expiración configurable
- **Seguridad por Roles**: Acceso diferenciado entre ADMIN y CLIENTE 
- **Historial de Transacciones**: Todas las operaciones quedan registradas
- **Tests de Cinco Tipos**: Unitarios, integración, aceptación, rendimiento y cliente JS
- **Cobertura Verificada**: JaCoCo con mínimo del 50 % de instrucciones
- **Frontend Completo**: Interfaz web HTML/CSS/JS integrada en la aplicación

### Alcance del Proyecto

**Incluido:**
- Gestión completa de clientes
- Gestión de cuentas bancarias
- Sistema de autenticación con JWT (login, roles, filtro por cabecera)
- Depósito y retiro de dinero
- Historial de transacciones
- Sistema de tests unitarios, de integración, aceptación, rendimiento y JavaScript
- Consulta de saldos con control de acceso
- Transferencias entre cuentas con validación de propietario
- Cobertura de código con JaCoCo
- Frontend integrado con formularios modales


---

##  Especificaciones Técnicas

### Stack Tecnológico

| Componente              | Tecnología          | Versión |
|-------------------------|---------------------|---------|
| **Framework**           | Spring Boot         | 4.0.3 |
| **Java**                | OpenJDK             | 25 |
| **Base de Datos**       | MySQL               | 8.0 |
| **ORM**                 | Hibernate/JPA       | Incluido en Spring |
| **Build Tool**          | Maven               | 3.9.12 |
| **Testing**             | JUnit 5, Mockito    | Latest |
| **Testing Rendimiento** | ContiPerf + JUnit 4 | 2.3.4 |
| **Cobertura**           | JaCoCo              | 0.8.13 |
| **Testing JS**          | Jest + node-fetch   | — |
| **Documentación API**   | Springdoc OpenAPI   | 3.0.2 |
| **Validación**          | Jakarta Validation  | Incluido en Spring |
| **Logging**             | SLF4J + Log4J2      | 2.25.3 |
| **Lombook**             | -                   | 1.18.42|

### Requisitos del Sistema

**Software Requerido:**
- Docker & Docker Compose (Recomendado)
- Java 25+
- Maven 3.9+
- MySQL 8.0+ (o usar Docker)

### Versiones de Dependencias Clave

```xml
<!-- Spring Boot Parent -->
<version>4.0.3</version>

<!-- Spring Security -->
spring-boot-starter-security
 
<!-- JWT (JJWT) -->
io.jsonwebtoken:jjwt-api:0.12.6
io.jsonwebtoken:jjwt-impl:0.12.6
io.jsonwebtoken:jjwt-jackson:0.12.6
 
<!-- Testing -->
spring-boot-starter-test (JUnit 5 + Mockito)
spring-security-test
junit:junit:4.13.2  (para ContiPerf)
org.junit.vintage:junit-vintage-engine
org.databene:contiperf:2.3.4
 
<!-- Cobertura -->
org.jacoco:jacoco-maven-plugin:0.8.13
 
<!-- API Docs -->
org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2
 
<!-- Lombok -->
org.projectlombok:lombok
 
<!-- Logging -->
spring-boot-starter-log4j2
```

---

##  Arquitectura del Sistema

### Patrones de Diseño

#### 1. Patrón MVC (Model-View-Controller)
- **Model**: Entidades JPA (Cliente, Cuenta, Transacción, Usuario)
- **Controller**: AuthController, ClienteController, CuentaController
- **View**: API REST (JSON) + Frontend HTML/JS estático

#### 2. Patrón Repository
```
Controller → Service → Repository → Database
```

#### 3. Patrón DTO (Data Transfer Object)
- ClienteRequest/ClienteResponse
- CuentaRequest/CuentaResponse
- DepositoRequest, RetiroRequest
- TransferenciaDTO
- SaldoResponse
- LoginRequest / LoginResponse

### Flujo de Datos

```
Cliente HTTP Request
          ↓
    JwtAuthFilter (valida JWT, carga contexto de seguridad)
         ↓
    Spring Filter (Validación)
         ↓
    Controller (Mapeo de rutas)
         ↓
    Service (Lógica de negocio)
         ↓
    Repository (Acceso a datos)
         ↓
    Base de Datos MySQL
         ↓
    Response JSON
```

### Capas de la Aplicación

```
┌──────────────────────────────────────────────────┐
│    Capa de Seguridad                             │
│  - JwtAuthFilter (lectura y validación de JWT)   │
│  - SecurityConfig (reglas de acceso por ruta)    │
│  - UserDetailsServiceImpl (carga usuarios de BD) │
│  - JwtUtils (generación y parseo de tokens)      │
└────────────┬─────────────────────────────────────┘
             ↓
┌──────────────────────────────────────────────────┐
│    Capa de Presentación (Endpoints)              │
│  - AuthController    (/api/auth)                 │
│  - ClienteController (/api/clientes)             │
│  - CuentaController (/cuentas)                   │
└────────────┬─────────────────────────────────────┘
             ↓
┌─────────────────────────────────────────────────┐
│    Capa de Negocio (Services)                   │
│  - ClienteService                               │
│  - CuentaService                                │
│  - TransferService                              │
└────────────┬────────────────────────────────────┘
             ↓
┌─────────────────────────────────────────────────┐
│    Capa de Datos (Repositories)                 │
│  - IClienteRepository                           │
│  - ICuentaRepository                            │
│  - ITransaccionRepository                       │
│  - IUsuarioRepository                           │
└────────────┬────────────────────────────────────┘
             ↓
┌─────────────────────────────────────────────────┐
│    Base de Datos (MySQL)                        │
│  - Tabla usuario                                │
│  - Tabla cliente                                │
│  - Tabla cuenta                                 │
│  - Tabla transaccion                            │
└─────────────────────────────────────────────────┘
```

### Relaciones entre Entidades

```
Usuario (1) ─────────── (0..1) Cliente, porque CLIENTE tiene clienteId, pero ADMIN tiene null
Cliente (1) ─────────── (N) Cuenta
Cuenta (1) ─────────── (N) Transaccion
```

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
# Nombre de la aplicación
spring.application.name=banca-online
 
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3307/banca_online?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=banca_user
spring.datasource.password=mi_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
 
# Hibernate/JPA
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
 
# Logging
logging.level.org.springframework.jdbc.datasource.init=DEBUG
logging.level.org.hibernate.SQL=DEBUG
 
# JWT
app.jwt.secret=dGhpcytlc3R1bmErc2VjcmV0YStjbGF2ZSttdXkrbGFyZ2E=
app.jwt.expiration-ms=3600000
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

- **Aplicación**: 8080 
- **MySQL (Docker)**: 3307 (externo) → 3306 (interno)


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

#### Tabla: usuario
```sql
CREATE TABLE IF NOT EXISTS usuario (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,          
    rol      ENUM('ADMIN','CLIENTE') NOT NULL,
    activo   BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id BIGINT NULL                   -- NULL para ADMIN
);
```

#### Tabla: cliente
```sql
CREATE TABLE IF NOT EXISTS cliente (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni              VARCHAR(20) UNIQUE NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    primer_apellido  VARCHAR(100),
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    email            VARCHAR(100) NOT NULL,
    telefono         VARCHAR(20),
    direccion        VARCHAR(200),
    fecha_creacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

#### Tabla: cuenta
```sql
CREATE TABLE IF NOT EXISTS cuenta (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id    BIGINT NOT NULL,
    numero_cuenta VARCHAR(30) UNIQUE NOT NULL,
    tipo_cuenta   ENUM('CORRIENTE','AHORRO') NOT NULL,
    saldo         DOUBLE NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);
```

#### Tabla: transaccion
```sql
CREATE TABLE IF NOT EXISTS transaccion (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo             ENUM('DEPOSITO','RETIRO','TRANSFERENCIA') NOT NULL,
    descripcion      VARCHAR(200),
    total            DECIMAL(10,2) NOT NULL,
    cuenta_origen_id BIGINT,
    cuenta_destino_id BIGINT,
    fecha            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cuenta_origen_id)  REFERENCES cuenta(id),
    FOREIGN KEY (cuenta_destino_id) REFERENCES cuenta(id)
);
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
   - `BCrypt` para almacenamiento de contraseñas

3. **Manejo de Excepciones**
   - Controllers validan y devuelven códigos HTTP apropiados
   - Services lanzan excepciones descriptivas
   - Errores de negocio se capturan y manejan

4. **Implementación JWT**
 
     La seguridad se implementa con **Spring Security** en modo stateless. El flujo completo es:
     
     1. **Login** (`POST /api/auth/login`): El `AuthController` delega en el `AuthenticationManager` de Spring Security, que usa `UserDetailsServiceImpl` para cargar el usuario desde BD y BCrypt para verificar la contraseña.
     2. **Generación del token**: Si la autenticación es correcta, `JwtUtils.generarToken()` crea un JWT con `sub=email`, `rol` y `clienteId` como claims, firmado con HMAC-SHA256.
     3. **Validación por filtro**: En cada petición, `JwtAuthFilter` lee la cabecera `Authorization: Bearer <token>`, valida el token con `JwtUtils.esValido()`, extrae el email y carga el contexto de seguridad (`SecurityContextHolder`).
     4. **Control de acceso**: Los controladores usan `@PreAuthorize("hasRole('ADMIN')")` o `@PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")`. Adicionalmente, los servicios verifican que el `clienteId` del token coincida con el recurso solicitado.


##  Frontend
 
La aplicación incluye un frontend completo servido como recursos estáticos desde `src/main/resources/static/`.
 
### Estructura
 
```
static/
├── index.html                  # Página principal
├── css/
│   ├── base.css
│   ├── estilo.css
│   ├── admin.css
│   ├── cliente.css
│   ├── componentes.css
│   └── modales.css
├── js/
│   ├── app.js                  # Lógica principal y navegación
│   ├── auth.js                 # Login y gestión del JWT
│   ├── modales.js              # Carga de modales
│   └── servicios/
│       ├── listarClientes.js
│       ├── crearClienteForm.js
│       ├── editarClienteForm.js
│       ├── eliminarClienteForm.js
│       ├── listarCuentas.js
│       ├── crearCuentaForm.js
│       ├── detalleCuenta.js
│       ├── consultarSaldo.js
│       ├── depositoForm.js
│       ├── retiroForm.js
│       ├── transferenciaForm.js
│       └── utils.js
└── modales/
    ├── listarClientes.html
    ├── crearClienteForm.html
    ├── editarClienteForm.html
    ├── eliminarClienteForm.html
    ├── listarCuentas.html
    ├── crearCuentaForm.html
    ├── detalleCuenta.html
    ├── consultarSaldo.html
    ├── depositoForm.html
    ├── retiroForm.html
    └── transferenciaForm.html
```
 
### Funcionamiento
 
El frontend usa `fetch` con la cabecera `Authorization: Bearer <token>` en todas las llamadas a la API. El token se obtiene al hacer login y se almacena en la sesión del cliente. Los formularios se cargan dinámicamente como modales HTML.
 
---

##  Estrategia de Testing
 
### Resumen de cobertura
 
| Capa | Test | Herramienta |
|------|------|-------------|
| Services | Unitarios | JUnit 5 + Mockito |
| Services | Aceptación | JUnit 5 + Mockito |
| Services | Rendimiento | ContiPerf + JUnit 4 |
| Controllers | Integración | Spring Boot Test + MockMvc |
| Repositories | Integración | Spring Boot Test |
| Cliente JS | Integración E2E | Jest + node-fetch |
| Cobertura global | — | JaCoCo (≥ 50 %) |
 
### Tests Unitarios
 
Prueban la lógica de negocio aislada mediante mocks de repositorios:
 
- `ClienteServiceTest` — creación, actualización, búsqueda, eliminación
- `CuentaServiceTest` — creación de cuentas, depósito, retiro, saldo, control de propietario
- `TransferServiceTest` — transferencias válidas, saldo insuficiente, acceso denegado
### Tests de Aceptación
 
`CuentaServiceAcceptanceTest` verifica criterios de aceptación desde perspectiva de negocio, con escenarios descritos con `@DisplayName` legibles.
 
### Tests de Rendimiento
 
`CuentaServicePerformanceTest` usa **ContiPerf** con anotaciones:
- `@PerfTest(invocations = N, threads = T)` — número de ejecuciones y hilos concurrentes
- `@Required(max = X)` — tiempo máximo permitido en milisegundos
Los informes se generan en `docs/reports/contiperf-report/index.html`.
 
### Tests de Integración
 
- `CuentaControllerIntegrationTest` — arranca el contexto Spring completo y prueba los endpoints de cuentas con MockMvc, incluyendo autenticación JWT.
- `ClienteControllerTest` — prueba los endpoints de clientes con MockMvc.
- `ClienteRepositoryTest` / `CuentaRepositoryTest` — prueban las consultas JPA sobre la base de datos MySQL con transacciones rollback.
### Tests de Cliente JavaScript
 
`src/test/js/client.test.js` realiza tests de integración end-to-end desde Node.js:
- Hace login real contra el servidor en `localhost:8080`
- Usa el token JWT obtenido para llamar a los endpoints de la API
- Valida respuestas HTTP y datos devueltos
**Ejecución:**
```bash
# Paso 1: asegurarse de que el servidor está corriendo
./mvnw spring-boot:run
 
# Paso 2: ejecutar los tests JS
cd src/test/js
npm install    # solo la primera vez
npm test
```
 
### Cobertura con JaCoCo
 
```bash
./mvnw test
# Informe en: target/site/jacoco/index.html
# O en la carpeta: docs/reports/site/jacoco/
```
 
El umbral mínimo configurado es **50 % de instrucciones** a nivel de bundle. Si no se alcanza, el build falla en la fase `verify`.
 

 ---


 
##  Autores

**Equipo de Desarrollo Banca Online:**
Eneko Barbadillo, Alberto García, Haizea González, Nora Ibarguren e Imanol Ugarte.

- **Repositorio**: [GitHub](https://github.com/PSyC25-26/PSyC-SS-02)
- **Proyecto**: [GitHub Project](https://github.com/orgs/PSyC25-26/projects/7)

##  Última Actualización

- **Fecha**: 20 de Abril de 2026
- **Versión**: 2.0.0 (Sprint 2)
- **Estado**: Funcional

---

**¡Gracias por usar Banca Online!**


