#!/bin/bash
export JAVA_HOME=$(dirname $(dirname $(find /usr/lib/jvm -name java | grep openjdk-21)))
echo "JAVA_HOME set to $JAVA_HOME"
./mvnw clean package
	