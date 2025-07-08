#!/bin/bash
if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q "21"; then
    echo "Downloading OpenJDK 21..."
    curl -L -o openjdk.tar.gz https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jdk_x64_linux_hotspot_21.0.2_13.tar.gz
    if [ $? -ne 0 ]; then
        echo "Failed to download OpenJDK 21"
        exit 1
    fi
    tar -xzf openjdk.tar.gz
    if [ $? -ne 0 ]; then
        echo "Failed to extract OpenJDK 21"
        exit 1
    fi
    mv jdk-21.0.2+13 jdk-21.0.2
    export JAVA_HOME=$(pwd)/jdk-21.0.2
    export PATH=$JAVA_HOME/bin:$PATH
    rm openjdk.tar.gz
fi
echo "JAVA_HOME set to $JAVA_HOME"
java -version || { echo "Java not found after setup"; exit 1; }

echo "Installing Maven..."
curl -L -o maven.tar.gz https://downloads.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar -xzf maven.tar.gz
mv apache-maven-3.9.9 maven
export PATH=$(pwd)/maven/bin:$PATH
rm maven.tar.gz
mvn --version || { echo "Maven not found after setup"; exit 1; }

echo "Building with Maven..."
mvn clean package || { echo "Maven build failed"; exit 1; }