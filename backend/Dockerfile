FROM gradle:8.13-jdk21-alpine as build

WORKDIR /home/gradle/project

# Cache Gradle daemon + wrapper
COPY --chown=gradle:gradle gradlew gradlew.bat ./
COPY --chown=gradle:gradle gradle/ ./gradle/

# Cache dependencies first (fast layer)
COPY --chown=gradle:gradle build.gradle settings.gradle ./
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle dependencies --no-daemon

# Cache source + build
COPY --chown=gradle:gradle src/ ./src/
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
