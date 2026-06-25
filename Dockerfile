<<<<<<< HEAD
# --- ETAPA 1: Construcción (Build) ---
# Usamos Maven con JDK 17 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias (optimiza el tiempo de build)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y generamos el archivo .jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: Ejecución (Runtime) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el archivo JAR generado desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Optimizaciones para contenedores: 
# -XX:+UseContainerSupport: (Activado por defecto en Java 10+) Asegura que la JVM respete los límites de memoria del contenedor.
# -XX:MaxRAMPercentage: Define el uso de RAM basado en el límite del contenedor en lugar de un valor fijo.
# -XshowSettings:vm: Muestra la configuración de la VM al inicio (útil para depuración en logs).
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XshowSettings:vm"

# Exponemos el puerto estándar de Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
=======
# ── Build stage ───────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

# ── Runtime stage ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
>>>>>>> develop
ENTRYPOINT ["java", "-jar", "app.jar"]
