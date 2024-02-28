FROM openjdk:17-jre-slim

WORKDIR /app

COPY pom.xml mvn:3-maven /app
RUN mvn package

COPY target/*.jar /app

EXPOSE 8081

CMD ["java", "-jar", "target/*.jar"]