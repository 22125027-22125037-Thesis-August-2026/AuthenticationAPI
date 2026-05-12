#!/bin/sh
# Entrypoint that handles JWT keys correctly by unescaping \n escape sequences

# Unescape literal \n to actual newlines in JWT keys
if [ ! -z "$JWT_PRIVATE_KEY" ]; then
  JWT_PRIVATE_KEY=$(printf '%b' "$JWT_PRIVATE_KEY")
fi

if [ ! -z "$JWT_PUBLIC_KEY" ]; then
  JWT_PUBLIC_KEY=$(printf '%b' "$JWT_PUBLIC_KEY")
fi

# Start Java with JWT keys passed as system properties
exec java -Dspring.profiles.active=docker \
  -Dmhsa.app.jwtPrivateKey="$JWT_PRIVATE_KEY" \
  -Dmhsa.app.jwtPublicKey="$JWT_PUBLIC_KEY" \
  -jar /app/app.jar
