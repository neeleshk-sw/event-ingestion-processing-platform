FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy all POMs
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

# 2. Cache dependencies
RUN mvn dependency:go-offline -B -pl routing-service -am

# 3. Copy source
COPY common/src common/src
COPY routing-service/src routing-service/src

# 4. Build
RUN mvn clean package -DskipTests -pl routing-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/routing-service/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
