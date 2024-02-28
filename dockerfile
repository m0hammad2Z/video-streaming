FROM maven:3.6.3-jdk-11 as build
ARG SERVICE_DIR
WORKDIR /app
COPY ${SERVICE_DIR}/pom.xml .
COPY ${SERVICE_DIR}/src ./src
RUN mvn clean package

FROM openjdk:17-slim
COPY --from=build /app/target/*.jar /app.jar

COPY wait.sh .
RUN chmod +x wait.sh