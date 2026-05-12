FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from Maven
COPY target/auth-service.jar app.jar

EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Use spring.profiles.active=docker to load application-docker.properties
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=docker -Dmhsa.app.jwtPrivateKey=\"$JWT_PRIVATE_KEY\" -Dmhsa.app.jwtPublicKey=\"$JWT_PUBLIC_KEY\" -jar app.jar"]
