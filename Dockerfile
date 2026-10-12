# ================================
# Base stage - Common dependencies
# ================================
FROM eclipse-temurin:25-jdk AS base

WORKDIR /app

COPY --chown=1000:1000 gradlew settings.gradle.kts build.gradle.kts ./
COPY --chown=1000:1000 gradle gradle
RUN chmod +x gradlew

RUN --mount=type=cache,id=gradle,target=/root/.gradle \
    ./gradlew --no-daemon dependencies

COPY --chown=1000:1000 src src

# ================================
# Development stage
# ================================
FROM base AS dev

EXPOSE 8081
CMD ["./gradlew", "bootRun", "--no-daemon", "--args=--spring.profiles.active=dev"]

# ================================
# Build stage - Compile
# ================================
FROM base AS build

RUN --mount=type=cache,id=gradle,target=/root/.gradle \
    ./gradlew --no-daemon bootJar -x test

# ================================
# Production stage - Optimized
# ================================
FROM eclipse-temurin:25-jre AS prod

WORKDIR /app

COPY --chown=1000:1000 --from=build /app/build/libs/*.jar app.jar
RUN mkdir -p /app/logs && chown 1000:1000 /app/logs

USER 1000

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
