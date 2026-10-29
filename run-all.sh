#!/usr/bin/env bash
set -e

echo "Starting Server in background..."
./gradlew :server:run &
SERVER_PID=$!

trap "echo 'Stopping Server...'; kill $SERVER_PID 2>/dev/null || true" EXIT

echo "Waiting for Server to start (port 8070)..."
sleep 3

echo "Starting Desktop App..."
./gradlew :desktopApp:run
