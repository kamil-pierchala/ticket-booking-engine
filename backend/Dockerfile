# build JAR application using Maven & Eclipse Temurin JDK 22
FROM maven:3.9.8-eclipse-temurin-22 AS builder
WORKDIR /app

# cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# copy source code and build package skipping tests (tests rely on running integration db)
COPY src ./src
RUN mvn clean package -DskipTests

# minimal runtime image
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]