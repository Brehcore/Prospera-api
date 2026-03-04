# =========================================================================
# ESTÁGIO 1: BUILD
# =========================================================================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw package -DskipTests

# =========================================================================
# ESTÁGIO 2: RUN
# =========================================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Expõe a porta 8080 (padrão do Spring Boot)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]