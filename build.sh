#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
echo "JAVA_HOME set to $JAVA_HOME"
java -version || { echo "Java not found"; exit 1; }
./mvnw clean package
