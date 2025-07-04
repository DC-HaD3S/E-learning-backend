#!/bin/bash
export JAVA_HOME=$(pwd)/jdk-21.0.2
export PATH=$JAVA_HOME/bin:$PATH
if ! command -v java >/dev/null 2>&1; then
    echo "Java not found"
    exit 1
fi
if [ -z "$JWT_SECRET" ]; then
    echo "JWT_SECRET not set"
    exit 1
fi
if [ -z "$PORT" ]; then
    echo "PORT not set"
    exit 1
fi
echo "JAVA_HOME set to $JAVA_HOME"
echo "Starting on port $PORT"
java -version
java -jar target/e-learning-0.0.1-SNAPSHOT.jar --server.port=$PORT
