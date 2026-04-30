FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .

COPY auth-common/pom.xml ./auth-common/
COPY budget-service/pom.xml ./budget-service/
COPY identity-service/pom.xml ./identity-service/

RUN mvn install -N

COPY auth-common/src ./auth-common/src
RUN mvn -f auth-common/pom.xml install -DskipTests

COPY budget-service/src ./budget-service/src
RUN mvn -f budget-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/budget-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]