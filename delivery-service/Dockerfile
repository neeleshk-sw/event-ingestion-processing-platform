FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy all POMs to satisfy Maven reactor
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
RUN mvn dependency:go-offline -B -pl delivery-service -am
# 3. Copy source
COPY common/src common/src
COPY recovery-service/src recovery-service/src
COPY delivery-service/src delivery-service/src

# 4. Build
RUN mvn clean package -DskipTests -pl delivery-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/delivery-service/target/*.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
