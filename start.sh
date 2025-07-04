#!/bin/bash
export JAVA_HOME=$(pwd)/jdk-21.0.2
export PATH=$JAVA_HOME/bin:$PATH
if ! command -v java >/dev/null 2>&1; then
    echo "Java not found"
    exit 1
fi
echo "JAVA_HOME set to $JAVA_HOME"
java -version
java -jar target/e-learning-0.0.1-SNAPSHOT.jar
