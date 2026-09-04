ARG JAVA_VERSION=25

# ---- build ----------------------------------------------------------------
FROM --platform=$BUILDPLATFORM eclipse-temurin:${JAVA_VERSION}-jdk AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests package

# ---- extract --------------------------------------------------------------
FROM --platform=$BUILDPLATFORM eclipse-temurin:${JAVA_VERSION}-jre AS extract
WORKDIR /extract
COPY --from=build /build/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination .

# ---- runtime --------------------------------------------------------------
FROM eclipse-temurin:${JAVA_VERSION}-jre AS runtime

ARG UID=10001
RUN adduser --system \
       --uid "${UID}" \
       --home-dir "/nonexistent" \
       --shell "/usr/sbin/nologin" \
       --no-create-home \
       spring

WORKDIR /app

COPY --from=extract --chown=${UID}:${GID} /extract/app/dependencies/ ./
COPY --from=extract --chown=${UID}:${GID} /extract/app/spring-boot-loader/ ./
COPY --from=extract --chown=${UID}:${GID} /extract/app/snapshot-dependencies/ ./
COPY --from=extract --chown=${UID}:${GID} /extract/app/application/ ./

USER ${UID}

ARG VERSION=dev
ENV APP_VERSION=${VERSION}

EXPOSE 8080

# ExitOnOutOfMemoryError makes the JVM die on heap exhaustion so the
# orchestrator restarts it, rather than the pod limping along failing requests.
ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "org.springframework.boot.loader.launch.JarLauncher"]
