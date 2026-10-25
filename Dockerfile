# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — BUILD
# Uses the official Maven + JDK 21 image to compile and package the app.
# This stage is discarded in the final image, so build tools never ship to prod.
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Copy dependency manifests first — Docker layer-caches them so that
# a source-only change does NOT re-download the internet.
COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline -f pom.xml

# Copy the rest of the source and build a fat JAR (skip tests —
# tests already run in the CI workflow before the image is built).
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress package -DskipTests

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — RUNTIME
# Starts from a minimal JRE-only image (~75 MB vs ~350 MB for JDK).
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Run as a non-root user — security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy only the packaged JAR from the build stage
COPY --from=build /workspace/target/task-manager-*.jar app.jar

# Expose the port Spring Boot listens on
EXPOSE 8080

# Use exec form so the JVM receives OS signals (SIGTERM) correctly —
# this enables graceful shutdown.
ENTRYPOINT ["java", "-jar", "app.jar"]
