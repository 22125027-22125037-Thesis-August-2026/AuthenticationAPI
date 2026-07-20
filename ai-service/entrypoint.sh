#!/bin/sh
# Entrypoint for AI Service

# Unescape literal \n to actual newlines in encryption keys if provided
if [ ! -z "$MHSA_CHAT_AES_KEY" ]; then
  MHSA_CHAT_AES_KEY=$(printf '%b' "$MHSA_CHAT_AES_KEY")
fi

# AI verifies tokens; it never issues them.
#
# When a JWKS endpoint is configured it resolves Auth's public key by kid at runtime, so it
# needs no signing material at all. That has to be enforced *here*, not in compose: the shared
# .env is mounted via env_file and still carries JWT_PRIVATE_KEY, so an entrypoint that reads
# it hands this service the key that mints tokens no matter what the compose environment block
# says.
if [ -n "$MHSA_APP_JWKSENDPOINT" ]; then
  exec java -Dspring.profiles.active=docker \
    -Dmhsa.chat.aes.key="$MHSA_CHAT_AES_KEY" \
    -jar /app/app.jar
fi

# Fallback for a deployment with no JWKS endpoint: verify against the static public key,
# unescaped if it carries literal \n. The private key is passed in neither branch.
if [ ! -z "$JWT_PUBLIC_KEY" ]; then
  JWT_PUBLIC_KEY=$(printf '%b' "$JWT_PUBLIC_KEY")
fi

exec java -Dspring.profiles.active=docker \
  -Dmhsa.app.jwtPublicKey="$JWT_PUBLIC_KEY" \
  -Dmhsa.chat.aes.key="$MHSA_CHAT_AES_KEY" \
  -jar /app/app.jar
