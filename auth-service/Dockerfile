FROM eclipse-temurin:17-jre-alpine

ARG BUILD_TIMESTAMP
WORKDIR /app

# Copy the built JAR and entrypoint script from Maven
COPY target/auth-service.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Use the entrypoint script to handle JWT key unescaping
ENTRYPOINT ["/app/entrypoint.sh"]
