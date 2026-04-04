#  PSyC-SS-02: Banca Online - Sistema de Gestión Bancaria Digital

![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-orange)
![Maven](https://img.shields.io/badge/Maven-3.9-red)
![JUnit](https://img.shields.io/badge/JUnit-5-green)


**Una aplicación de banca en línea construida con Spring Boot para gestionar clientes, cuentas bancarias y transferencias de dinero.**

---

##  Tabla de Contenidos

- [Descripción](#descripción)
- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación Rápida Con Docker](#instalación-rápida-con-docker)
- [Endpoints API](#endpoints-api)
- [Estructura del Proyecto](#estructura-del-proyecto)
---

##  Descripción

**Banca Online** es una aplicación backend desarrollada con **Spring Boot** que proporciona una API REST completa para la gestión de operaciones bancarias digitales. El sistema gestiona:

-  **Clientes**: Crear, actualizar, buscar y eliminar información de clientes
-  **Cuentas Bancarias**: Crear y gestionar cuentas corrientes y de ahorro
-  **Transferencias**: Transferencias seguras entre cuentas
-  **Consultas de Saldo**: Verificación instantánea de disponibilidad

El proyecto está diseñado con una arquitectura de capas clara, incluye tests exhaustivos y está listo para producción.

---

##  Características

### Backend
-  **API REST Completa** - Endpoints CRUD para clientes y cuentas
-  **Base de Datos Relacional** - MySQL 8.0 con esquema bien definido
-  **Validación de Datos** - Jakarta Validation automático en DTOs
-  **Tests Exhaustivos** - Tests unitarios con JUnit 5 y Mockito
-  **DTOs** - Separación clara entre modelos internos y API
-  **Manejo de Excepciones** - Respuestas HTTP apropiadas para cada caso
-  **Swagger/OpenAPI** - Documentación interactiva de la API
-  **Docker Support** - Docker Compose para deployment simple

### Frontend
-  **Interfaz Web** - Formularios HTML/CSS/JavaScript
-  **Responsive Design** - Compatible con dispositivos móviles
-  **Integración AJAX** - Comunicación dinámica con la API
-  **Validación Frontend** - Validación antes de enviar al servidor

---

##  Requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop) (incluye Docker Compose)
- **Java 25+** - [Descargar](https://jdk.java.net/25/)
- **Maven 3.9+** - [Descargar](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [Descargar](https://dev.mysql.com/downloads/mysql/)
- **Git** - [Descargar](https://git-scm.com/)

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

 **Listo!** La aplicación estará disponible en:
- API: http://localhost:8080/api/clientes
- Swagger: http://localhost:8080/swagger-ui.html
- Formularios: http://localhost:8080/clienteForm.html

---

##  Endpoints API

### Clientes

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| POST | `/api/clientes` | Crear cliente | 201 |
| GET | `/api/clientes` | Listar clientes | 200 |
| GET | `/api/clientes/{id}` | Obtener cliente | 200 |
| GET | `/api/clientes/email/{email}` | Buscar por email | 200 |
| PUT | `/api/clientes/{id}` | Actualizar cliente | 200 |
| DELETE | `/api/clientes/{id}` | Eliminar cliente | 204 |

### Cuentas

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| POST | `/api/cuentas` | Crear cuenta | 201 |
| POST | `/api/cuentas/transferir` | Transferencia | 200 |
| POST | `/api/cuentas/retirar` | Retirar | 200 |
| POST | `/api/cuentas/depositar` | Depositar | 200 |
| GET | `/api/cuentas` | Listar cuentas | 200 |
| GET | `/api/cuentas/saldo/{cuentaId}` | Obtener saldo | 200 |


---


##  Documentación Completa

Para documentación técnica detallada, ver el archivo **DOCUMENTACION_BANCA_ONLINE.md** que incluye:

- Descripción del Proyecto
- Especificaciones Técnicas
- Arquitectura del Sistema
- Guía de Instalación
- Configuración
- Base de Datos
- Seguridad


##  Autores

**Equipo de Desarrollo Banca Online:**
Eneko Barbadillo, Alberto García, Haizea González, Nora Ibarguren e Imanol Ugarte.

- **Repositorio**: [GitHub](https://github.com/PSyC25-26/PSyC-SS-02)
- **Proyecto**: [GitHub Project](https://github.com/orgs/PSyC25-26/projects/7)

##  Última Actualización

- **Fecha**: 26 de Marzo de 2026
- **Versión**: 1.0.0 (Sprint 1)
- **Estado**: En desarrollo

---

**¡Gracias por usar Banca Online!**

