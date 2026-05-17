FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/ai-service.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
