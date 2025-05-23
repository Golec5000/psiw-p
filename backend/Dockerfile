# 1) 🔨 buduj JAR-a
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2) 🔑 generuj klucze RSA
FROM alpine:latest AS keys
RUN apk add --no-cache openssl
WORKDIR /keys
RUN openssl genrsa -out privateKey.pem 2048 \
 && openssl rsa -in privateKey.pem -pubout -out publicKey.pem

# 3) 🚀 obraz runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# skopiuj jar
COPY --from=build /app/target/*.jar app.jar
# skopiuj klucze
COPY --from=keys /keys/*.pem ./

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
