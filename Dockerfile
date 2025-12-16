FROM olehprukhnytskyi/base-java-otel:21
WORKDIR /app
COPY target/macro-tracker-notification-service-0.0.1-SNAPSHOT.jar macro-tracker-notification-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "macro-tracker-notification-service.jar"]