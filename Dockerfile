FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY apps/backend/pom.xml apps/backend/pom.xml
RUN mvn -B -f apps/backend/pom.xml dependency:go-offline
COPY apps/backend/src apps/backend/src
RUN mvn -B -f apps/backend/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 harborvoice
COPY --from=build --chown=harborvoice:harborvoice /workspace/apps/backend/target/restaurant-voice-platform-0.0.1-SNAPSHOT.jar /app/harborvoice.jar
USER harborvoice
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/harborvoice.jar"]
