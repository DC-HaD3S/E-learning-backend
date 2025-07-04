#!/bin/bash
for path in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/jdk-21 /usr/lib/jvm/openjdk-21 /usr/lib/jvm/temurin-21-jdk-amd64 /usr/lib/jvm/zulu-21; do
    if [ -d "$path" ]; then
        export JAVA_HOME=$path
        break
    fi
done
if [ -z "$JAVA_HOME" ] && command -v java >/dev/null 2>&1; then
    export JAVA_HOME=$(dirname $(dirname $(command -v java)))
fi
if [ -z "$JAVA_HOME" ]; then
    echo "Java 21 not found"
    exit 1
fi
echo "JAVA_HOME set to $JAVA_HOME"
java -version
./mvnw clean package
