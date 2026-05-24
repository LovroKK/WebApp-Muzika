# --- Stage 1: Build ---
# Koristimo Maven + JDK 17 sliku da izgradimo .jar.
# Slojevi su poredani tako da se Maven dependencies cache-iraju
# i ne preuzimaju se ponovno pri svakoj promjeni koda.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Prvo samo pom.xml -> cache za dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sada izvorni kod i build (preskačemo testove za brži build)
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: Runtime ---
# Lagana JRE slika - sadrži samo gotovi .jar, ne i Maven/build alate.
FROM eclipse-temurin:17-jre
WORKDIR /app

# Kopiramo izgrađeni .jar iz build stagea
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
