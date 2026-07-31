# ==============================================================================
# ☕ DEVSECOPS DOCKERFILE - BACKEND SPRING BOOT (JAVA 17)
# ==============================================================================
# Multi-stage build pour minimiser la taille de l'image et renforcer la sécurité
# ==============================================================================

# ------------------------------------------------------------------------------
# ÉTAPE 1 : Compilation et packaging (Build Stage)
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Cache des dépendances Maven (Copie explicite du pom.xml)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie des sources et compilation du JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# ÉTAPE 2 : Image d'exécution minimale et sécurisée (Runtime Stage)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="SIMPL-IS DevSecOps Team" \
      description="Backend Spring Boot - SIMPL-IS Tax Declaration Service"

WORKDIR /app

# Sécurité DevSecOps : Création d'un utilisateur et groupe non-root
RUN addgroup -S spring && adduser -S spring -G spring

# Copie du JAR depuis l'étape de compilation
COPY --from=builder /app/target/Declarations-fiscaux-0.0.1-SNAPSHOT.jar app.jar

# Attribution des permissions à l'utilisateur non-root
RUN chown -R spring:spring /app

# Utilisation systématique de l'utilisateur non-root
USER spring:spring

# Exposition du port de l'application Spring Boot
EXPOSE 8080

# Healthcheck natif Docker
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Exécution optimisée de l'application Java
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
