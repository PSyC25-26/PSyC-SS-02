#  PSyC-SS-02: Banca Online - Sistema de Gestión Bancaria Digital

![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-green)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-orange)
![Maven](https://img.shields.io/badge/Maven-3.9-red)
![JUnit](https://img.shields.io/badge/JUnit-5-green)
![Coverage](https://img.shields.io/badge/Coverage-50%25%2B-yellowgreen)

**Una aplicación de banca en línea construida con Spring Boot para gestionar clientes, cuentas bancarias y transferencias de dinero, con autenticación JWT y control de acceso por roles.**

---

##  Tabla de Contenidos

- [Descripción](#descripción)
- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación Rápida Con Docker](#instalación-rápida-con-docker)
- [Autenticación y Seguridad](#autenticación-y-seguridad)
- [Endpoints API](#endpoints-api)
- [Tests](#tests)
---

##  Descripción

**Banca Online** es una aplicación web completa desarrollada con **Spring Boot** que proporciona una API REST completa para la gestión de operaciones bancarias digitales. El sistema gestiona:

-  **Autenticación**: Login con email/contraseña y tokens JWT con expiración configurable
-  **Control de Acceso**: Dos roles diferenciados — `ADMIN` y `CLIENTE`
-  **Clientes**: Crear, actualizar, buscar y eliminar información de clientes
-  **Cuentas Bancarias**: Crear y gestionar cuentas corrientes y de ahorro
-  **Operaciones Financieras**: Depósito, retiro y transferencias entre cuentas
-  **Historial de Transacciones**: Registro persistente de todas las operaciones
-  **Consultas de Saldo**: Verificación instantánea con control de acceso por propietario

El proyecto está diseñado con una arquitectura de capas clara, incluye tests exhaustivos de múltiples tipos y cuenta con un frontend, y está listo para producción.

---
##  Requisitos
Para poder ejecutar los test del lado cliente de manera correcta es necesario tener instalado [Node.js](https://nodejs.org/es/download). 

---
##  Características

### Backend
-  **API REST Completa** — Endpoints CRUD para clientes, cuentas y operaciones financieras
-  **Autenticación JWT** — Tokens firmados con HMAC-SHA, claims de rol y clienteId
-  **Seguridad por Roles** — `ADMIN` gestiona todo; `CLIENTE` solo accede a sus propios recursos
-  **Base de Datos Relacional** — MySQL 8.0 con esquema bien definido (4 tablas)
-  **Historial de Transacciones** — Registro de depósitos, retiros y transferencias en tabla `transaccion`
-  **Validación de Datos** — Jakarta Validation automático en DTOs
-  **Tests Exhaustivos** — Unitarios, integración, aceptación, rendimiento y cliente JS
-  **Cobertura de Código** — JaCoCo con mínimo exigido del 50 %
-  **DTOs** — Separación clara entre modelos internos y API pública
-  **Manejo de Excepciones** — Respuestas HTTP apropiadas para cada caso de error
-  **Swagger/OpenAPI** — Documentación interactiva de la API
-  **Docker Support** — Docker Compose para despliegue simple
-  **Logging** — Log4J2 con configuración personalizada

### Frontend
-  **Interfaz Web** — Formularios HTML/CSS/JavaScript
-  **Responsive Design** — Compatible con dispositivos móviles
-  **Integración AJAX** — Comunicación dinámica con la API
-  **Validación Frontend** — Validación antes de enviar al servidor
-  **Gestión de Sesión** — Almacenamiento y uso del JWT en el cliente

---

##  Requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop) (incluye Docker Compose)
- **Java 25+** — [Descargar](https://jdk.java.net/25/)
- **Maven 3.9+** — [Descargar](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** — [Descargar](https://dev.mysql.com/downloads/mysql/) 
- **Git** — [Descargar](https://git-scm.com/)

---

##  Instalación Rápida Con Docker


1.  **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/banca-online.git
cd banca-online
```

2.  **Iniciar MySQL con Docker Compose**
```bash
docker-compose up -d
```

3.  **Ejecutar la aplicación**
```bash
./mvnw spring-boot:run
```

 **¡Listo!** La aplicación estará disponible en:
- Frontend: http://localhost:8080/index.html
- API base: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

---

##  Autenticación y Seguridad

La aplicación usa **Spring Security + JWT** con política de sesión stateless.

### Flujo de autenticación

1. El cliente envía `POST /api/auth/login` con email y contraseña.
2. El servidor valida las credenciales contra la tabla `usuario` .
3. Si son correctas, devuelve un JWT firmado que contiene el rol y el `clienteId`.
4. El cliente incluye el token en cada petición mediante la cabecera `Authorization: Bearer <token>`.
5. El filtro `JwtAuthFilter` valida el token y carga el contexto de seguridad antes de cada petición.

### Roles y permisos

| Rol | Acceso |
|-----|--------|
| `ADMIN` | Acceso total a todos los endpoints |
| `CLIENTE` | Puede consultar saldo, listar sus propias cuentas, depositar, retirar y transferir desde sus cuentas |

### Credenciales para test

| Rol | Correo | Contraseña |
|-----|--------|------------|
| Admin | `admin@banco.com` | `admin123` |
| Cliente | `cliente@banco.com` | `cliente123` |

### Endpoints públicos (sin autenticación)

- `POST /api/auth/login`
- `GET /`, `/index.html`, `/css/**`, `/js/**`, `/modales/**`
- `GET /swagger-ui/**`, `/v3/api-docs/**`

---

##  Endpoints API

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Login y obtención de JWT | Público |

**Body de login:**
```json
{ "email": "admin@banco.com", "password": "admin123" }
```
**Respuesta:**
```json
{ "token": "eyJ...", "rol": "ADMIN", "clienteId": null, "email": "admin@banco.com" }
```

---

### Clientes — Solo ADMIN

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| POST | `/api/clientes` | Crear cliente | 201 |
| GET | `/api/clientes` | Listar clientes | 200 |
| GET | `/api/clientes/{id}` | Obtener cliente | 200 |
| GET | `/api/clientes/email/{email}` | Buscar por email | 200 |
| PUT | `/api/clientes/{id}` | Actualizar cliente | 200 |
| DELETE | `/api/clientes/{id}` | Eliminar cliente | 204 |

### Cuentas

| Método | Endpoint | Descripción | Roles | Status |
|--------|----------|-------------|-------|--------|
| POST | `/api/cuentas` | Crear cuenta | ADMIN | 201 |
| POST | `/api/cuentas/transferir` | Transferir entre cuentas | ADMIN, CLIENTE | 200 |
| POST | `/api/cuentas/retirar` | Retirar dinero | ADMIN, CLIENTE | 200 |
| POST | `/api/cuentas/depositar` | Depositar dinero | ADMIN, CLIENTE | 200 |
| GET | `/api/cuentas?clienteId={id}` | Listar cuentas de un cliente | ADMIN | 200 |
| GET | `/api/cuentas/{clienteId}` | Listar cuentas propias | ADMIN, CLIENTE | 200 |
| GET | `/api/cuentas/saldo/{cuentaId}` | Consultar saldo | ADMIN, CLIENTE | 200 |

**Nota:** Un CLIENTE solo puede consultar o operar sobre las cuentas vinculadas a su `clienteId`. Si intenta acceder a cuentas de otro cliente, recibirá `403 Forbidden`.

---

##  Tests

El proyecto incluye cinco tipos de tests:

### Tests unitarios (JUnit 5 + Mockito)
- `ClienteServiceTest` : lógica de negocio de clientes
- `CuentaServiceTest` : lógica de cuentas y operaciones financieras
- `TransferServiceTest` : lógica de transferencias y control de acceso
- `ClienteRepositoryTest` : capa de repositorio de clientes
- `CuentaRepositoryTest` : capa de repositorio de cuentas

### Tests de integración (Spring Boot Test)
- `CuentaControllerIntegrationTest` : endpoints de cuentas con contexto Spring completo
- `ClienteControllerTest` : endpoints de clientes con MockMvc

### Tests de aceptación
- `CuentaServiceAcceptanceTest` : criterios de aceptación de las operaciones bancarias desde perspectiva de negocio

### Tests de rendimiento (ContiPerf + JUnit 4)
- `CuentaServicePerformanceTest` : mide tiempos de respuesta bajo carga con anotaciones `@PerfTest` y `@Required`

### Tests de cliente JavaScript (Jest + node-fetch)
- `src/test/js/client.test.js` : tests de integración que llaman al servidor desde Node.js simulando un cliente real

**Ejecución:**
```bash
# Tests Java
./mvnw test

# Para ejecutar los tests referentes al rendimiento y evitar saturaciones, es necesario ejecutarlos por separado. Por ejemplo:
mvn test -Dtest=CuentaServicePerformanceTest#rendimiento_crearCuenta_100invocaciones_4hilos


# Tests JavaScript (requiere servidor levantado en localhost:8080)
cd src/test/js
npm install
npm test
```


**Cobertura:** JaCoCo genera el informe en `docs/reports/site/jacoco/`. El mínimo exigido es del **50 % de instrucciones**.

**Ejecución de los tests del lado de cliente:**
- Por un lado la app de sprigboot debe estar ejecutándose:
```bash
# Ejecutar app springboot
docker-compose down -v

# Test lado servidor
mvn test

# Tests JavaScript (requiere servidor levantado en localhost:8080)
cd src/test/js
npm install
npm test
```

---

##  Documentación Completa

Para documentación técnica detallada, ver el archivo **DOCUMENTACION_BANCA_ONLINE.md** que incluye:

- Descripción del Proyecto
- Especificaciones Técnicas
- Arquitectura del Sistema
- Guía de Instalación
- Configuración
- Base de Datos
- Seguridady autenticación JWT
- Testing

---

##  Autores

**Equipo de Desarrollo Banca Online:**
Eneko Barbadillo, Alberto García, Haizea González, Nora Ibarguren e Imanol Ugarte.

- **Repositorio**: [GitHub](https://github.com/PSyC25-26/PSyC-SS-02)
- **Proyecto**: [GitHub Project](https://github.com/orgs/PSyC25-26/projects/7)

##  Última Actualización

- **Fecha**: 19 de Abril de 2026
- **Versión**: 2.0.0 (Sprint 2)
- **Estado**: Funcional

---

**¡Gracias por usar Banca Online!**
