#!/bin/sh
set -e

# Dashboard verifies tokens; it never issues them.
#
# When a JWKS endpoint is configured it resolves Auth's public key by kid at runtime, so it
# needs no signing material at all. That has to be enforced *here*, not in compose: the shared
# .env is mounted via env_file and still carries JWT_PRIVATE_KEY, so an entrypoint that reads
# it hands this service the key that mints tokens no matter what the compose environment block
# says.
if [ -n "$MHSA_APP_JWKSENDPOINT" ]; then
  exec java -Dspring.profiles.active=docker -jar dashboard-service.jar
fi

# Fallback for a deployment with no JWKS endpoint: verify against the static public key,
# unescaped if it carries literal \n. The private key is passed in neither branch.
if [ -n "$JWT_PUBLIC_KEY" ]; then
  export MHSA_APP_JWTPUBLICKEY=$(printf '%b' "$JWT_PUBLIC_KEY")
fi

exec java -Dspring.profiles.active=docker \
  -Dmhsa.app.jwtPublicKey="$MHSA_APP_JWTPUBLICKEY" \
  -jar dashboard-service.jar
