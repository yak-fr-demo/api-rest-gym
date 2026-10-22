# Fase 1: Compilar la aplicación con Maven
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos primero los archivos de dependencias y configuración de Maven
# Esto ayuda a aprovechar la caché de Docker y agilizar los siguientes builds
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Damos permisos de ejecución al mvnw y descargamos las dependencias
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copiamos el código fuente restante
COPY src ./src

# Compilamos el proyecto omitiendo las pruebas
RUN ./mvnw clean package -DskipTests

# Fase 2: Ejecutar la aplicación ligera
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR generado desde la primera fase
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]