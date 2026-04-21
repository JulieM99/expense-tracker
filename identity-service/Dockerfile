FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .

COPY identity-service/pom.xml ./identity-service/
COPY identity-service/src ./identity-service/src

RUN mvn -f identity-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/identity-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]