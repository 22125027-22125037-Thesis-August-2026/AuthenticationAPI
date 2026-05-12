#!/bin/sh
# Proper entrypoint that handles JWT keys correctly

JAVA_OPTS="-Dspring.profiles.active=docker"

# Unescape literal \n to actual newlines in JWT keys (Docker env vars have escaped newlines)
if [ ! -z "$JWT_PRIVATE_KEY" ]; then
  JWT_PRIVATE_KEY=$(echo "$JWT_PRIVATE_KEY" | sed 's/\\n/\n/g')
  JAVA_OPTS="$JAVA_OPTS -Dmhsa.app.jwtPrivateKey=$JWT_PRIVATE_KEY"
fi

if [ ! -z "$JWT_PUBLIC_KEY" ]; then
  JWT_PUBLIC_KEY=$(echo "$JWT_PUBLIC_KEY" | sed 's/\\n/\n/g')
  JAVA_OPTS="$JAVA_OPTS -Dmhsa.app.jwtPublicKey=$JWT_PUBLIC_KEY"
fi

exec java $JAVA_OPTS -jar /app/app.jar
