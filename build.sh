#!/bin/bash
# Build all Maven modules using Docker

docker run --rm -v "$(cd "$(dirname "$0")" && pwd)":/workspace -w /workspace maven:3.9-eclipse-temurin-17 \
  mvn clean package -DskipTests

echo "Build complete!"
