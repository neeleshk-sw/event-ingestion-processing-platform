FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy all POMs to satisfy reactor and cache dependencies
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY intake-service/pom.xml intake-service/pom.xml
COPY validation-service/pom.xml validation-service/pom.xml
COPY normalization-service/pom.xml normalization-service/pom.xml
COPY enrichment-service/pom.xml enrichment-service/pom.xml
COPY routing-service/pom.xml routing-service/pom.xml
COPY delivery-service/pom.xml delivery-service/pom.xml
COPY recovery-service/pom.xml recovery-service/pom.xml
COPY audit-service/pom.xml audit-service/pom.xml

# 2. Download dependencies (this layer is cached until poms change)
RUN mvn dependency:go-offline -B -pl intake-service -am

# 3. Copy shared source and service source
COPY common/src common/src
COPY intake-service/src intake-service/src

# 4. Build the service
RUN mvn clean package -DskipTests -pl intake-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/intake-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
