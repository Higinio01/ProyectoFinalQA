# Etapa de build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar archivos de configuración y Gradle Wrapper
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copiar código fuente
COPY src src

# Dar permisos y construir el proyecto (incluye test + cucumber reports)
RUN chmod +x gradlew
RUN ./gradlew clean test build

# Copiar reportes (si existen)
RUN mkdir -p /app/test-results && \
    if [ -d build/cucumber-reports ]; then cp -r build/cucumber-reports /app/test-results; fi

# Etapa de runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer el puerto de Spring Boot
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
