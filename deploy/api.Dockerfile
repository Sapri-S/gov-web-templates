# Build from the repository root:
#   docker build -f deploy/api.Dockerfile -t portal-api .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY apps/portal-api/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY apps/portal-api/src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --no-create-home app
WORKDIR /app
COPY --from=build /src/target/portal-api-*.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
