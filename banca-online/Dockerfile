# ============================================================================
# Banca Online - Dockerfile multi-stage
# ============================================================================
# Stage 1 (builder): compila el proyecto con Maven y Java 25
# Stage 2 (runtime): imagen final ligera con solo el JAR ejecutable
# ============================================================================

# ---------- Stage 1: Build ----------
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Copiamos primero los ficheros de configuracion de Maven para aprovechar el cache de Docker
COPY .mvn/ .mvn
COPY mvnw mvnw.cmd pom.xml ./

# Damos permisos al wrapper y descargamos dependencias (capa cacheable)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiamos el codigo fuente y compilamos
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copiamos solo el JAR construido (mucho mas ligero que el JDK completo)
COPY --from=builder /build/target/*.jar app.jar

# Puerto en el que escucha Spring Boot
EXPOSE 8080

# Variables de entorno con defaults (sobreescribibles desde docker-compose)
ENV DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=banca_online \
    DB_USER=banca_user \
    DB_PASSWORD=mi_password

ENTRYPOINT ["java", "-jar", "app.jar"]