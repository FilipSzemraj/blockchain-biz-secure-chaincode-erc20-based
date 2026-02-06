FROM openjdk:21-slim

WORKDIR /app

COPY build/libs/bankAPI-0.0.1-SNAPSHOT-plain.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
