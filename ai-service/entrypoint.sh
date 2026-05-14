#!/bin/sh
# Entrypoint for AI Service

# Unescape literal \n to actual newlines in encryption keys if provided
if [ ! -z "$MHSA_CHAT_AES_KEY" ]; then
  MHSA_CHAT_AES_KEY=$(printf '%b' "$MHSA_CHAT_AES_KEY")
fi

# Start Java with keys and environment passed as system properties
exec java -Dspring.profiles.active=docker \
  -Dmhsa.chat.aes.key="$MHSA_CHAT_AES_KEY" \
  -jar /app/app.jar
