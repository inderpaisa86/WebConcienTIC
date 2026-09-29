FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace
COPY . .

RUN chmod +x gradlew && \
    for attempt in 1 2 3 4 5; do \
      echo "Gradle build attempt ${attempt}/5"; \
      ./gradlew clean bootJar --no-daemon && exit 0; \
      if [ "$attempt" -lt 5 ]; then \
        delay=$((attempt * 20)); \
        echo "Gradle build failed; retrying in ${delay}s"; \
        sleep "$delay"; \
      fi; \
    done; \
    exit 1

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /workspace/build/libs/concientic-catalog-0.1.0-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
