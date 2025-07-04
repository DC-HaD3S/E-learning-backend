#!/bin/bash
# Install Java 21 if not present
if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q "21"; then
    echo "Installing OpenJDK 21..."
    apt-get update
    apt-get install -y openjdk-21-jdk
fi
# Set JAVA_HOME
export JAVA_HOME=$(dirname $(dirname $(command -v java)))
echo "JAVA_HOME set to $JAVA_HOME"
# Verify Java version
java -version
# Run Maven build
./mvnw clean package