#!/bin/bash
if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q "21"; then
    echo "Downloading OpenJDK 21..."
    curl -L -o openjdk.tar.gz https://download.java.net/java/GA/jdk21.0.2/91a3c1b5f2/12/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz
    tar -xzf openjdk.tar.gz
    export JAVA_HOME=$(pwd)/jdk-21.0.2
    export PATH=$JAVA_HOME/bin:$PATH
    rm openjdk.tar.gz
fi
echo "JAVA_HOME set to $JAVA_HOME"
java -version
echo "Skipping Maven build, using pre-built JAR"
