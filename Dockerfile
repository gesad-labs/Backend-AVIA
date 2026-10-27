# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline

COPY src src
COPY VERSION VERSION
# O application.properties real não é versionado; parte-se do exemplo e todo valor
# sensível chega por variável de ambiente (SPRING_DATASOURCE_*, JWT_SECRET, MAIL_*).
RUN cp src/main/resources/application-example.properties src/main/resources/application.properties \
    && printf '\napp.version=%s\n' "$(cat VERSION)" >> src/main/resources/application.properties
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests package \
    && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --no-create-home avia
WORKDIR /app
COPY --from=build /app/app.jar app.jar
USER avia
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
