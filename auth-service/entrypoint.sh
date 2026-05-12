#!/bin/sh
# Proper entrypoint that handles JWT keys correctly

JAVA_OPTS="-Dspring.profiles.active=docker"

# If JWT keys are provided via env vars, add them as system properties
if [ ! -z "$JWT_PRIVATE_KEY" ]; then
  JAVA_OPTS="$JAVA_OPTS -Dmhsa.app.jwtPrivateKey=$JWT_PRIVATE_KEY"
fi

if [ ! -z "$JWT_PUBLIC_KEY" ]; then
  JAVA_OPTS="$JAVA_OPTS -Dmhsa.app.jwtPublicKey=$JWT_PUBLIC_KEY"
fi

exec java $JAVA_OPTS -jar /app/app.jar
