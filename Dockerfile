# 1. Usamos una imagen de Maven para CONSTRUIR la app (Etapa de Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Usamos una imagen ligera de Java para CORRER la app (Etapa de Run)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# 3. ¡LA CLAVE! Forzamos la variable del puerto aquí mismo
ENV SERVER_PORT=8080
# Le decimos a Railway explícitamente que este contenedor usa el 8080
EXPOSE 8080

# 4. Arrancamos la app
ENTRYPOINT ["java", "-jar", "app.jar"]