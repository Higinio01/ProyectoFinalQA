FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY src src

RUN chmod +x gradlew
RUN ./gradlew clean test build

RUN mkdir -p /app/test-results && \
    if [ -d build/cucumber-reports ]; then cp -r build/cucumber-reports /app/test-results; fi

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
