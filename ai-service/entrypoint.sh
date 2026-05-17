#!/bin/sh
# Entrypoint for AI Service

# Unescape literal \n to actual newlines in JWT keys
if [ ! -z "$JWT_PRIVATE_KEY" ]; then
  JWT_PRIVATE_KEY=$(printf '%b' "$JWT_PRIVATE_KEY")
fi

if [ ! -z "$JWT_PUBLIC_KEY" ]; then
  JWT_PUBLIC_KEY=$(printf '%b' "$JWT_PUBLIC_KEY")
fi

# Unescape literal \n to actual newlines in encryption keys if provided
if [ ! -z "$MHSA_CHAT_AES_KEY" ]; then
  MHSA_CHAT_AES_KEY=$(printf '%b' "$MHSA_CHAT_AES_KEY")
fi

# Start Java with keys and environment passed as system properties
exec java -Dspring.profiles.active=docker \
  -Dmhsa.app.jwtPrivateKey="$JWT_PRIVATE_KEY" \
  -Dmhsa.app.jwtPublicKey="$JWT_PUBLIC_KEY" \
  -Dmhsa.chat.aes.key="$MHSA_CHAT_AES_KEY" \
  -jar /app/app.jar
