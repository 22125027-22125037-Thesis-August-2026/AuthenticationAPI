#!/bin/sh
# Entrypoint for Tracking Service

exec java -Dspring.profiles.active=docker -jar /app/app.jar
