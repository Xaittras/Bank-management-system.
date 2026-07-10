# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Спершу тільки pom.xml — Docker закешує залежності окремим шаром,
# і якщо код зміниться, а pom.xml ні, mvn dependency:go-offline не перезапускатиметься.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
