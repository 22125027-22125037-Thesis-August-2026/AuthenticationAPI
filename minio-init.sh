#!/bin/bash

# Wait for MinIO to be ready
sleep 5

# Create bucket using mc (MinIO client)
mc alias set minio http://minio:9000 minioadmin minioadmin
mc mb minio/mhsa-media --ignore-existing

echo "MinIO bucket 'mhsa-media' initialized"
