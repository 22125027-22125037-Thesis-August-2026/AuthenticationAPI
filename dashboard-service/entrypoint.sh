#!/bin/sh
set -e

# Unescape JWT keys if they contain literal \n
if [ -n "$JWT_PRIVATE_KEY" ]; then
  export MHSA_APP_JWTPRIVATEKEY=$(printf '%b' "$JWT_PRIVATE_KEY")
fi

if [ -n "$JWT_PUBLIC_KEY" ]; then
  export MHSA_APP_JWTPUBLICKEY=$(printf '%b' "$JWT_PUBLIC_KEY")
fi

# Run the application
exec java -Dspring.profiles.active=docker \
  -Dmhsa.app.jwtPrivateKey="$MHSA_APP_JWTPRIVATEKEY" \
  -Dmhsa.app.jwtPublicKey="$MHSA_APP_JWTPUBLICKEY" \
  -jar dashboard-service.jar
