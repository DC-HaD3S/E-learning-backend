#!/bin/bash
# Download and set up OpenJDK 21 if not present
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

# Check Java
if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java not found in $JAVA_HOME/bin"
    exit 1
fi
if ! command -v javac >/dev/null 2>&1; then
    echo "Error: javac not found in $JAVA_HOME/bin. JAVA_HOME must point to a JDK, not a JRE"
    exit 1
fi

# Log Java version
echo "JAVA_HOME set to $JAVA_HOME"
java -version || { echo "Java not found after setup"; exit 1; }

# Copy pre-built JAR to target/
PREBUILT_JAR="artifacts/e-learning-0.0.1-SNAPSHOT.jar"
TARGET_JAR="target/e-learning-0.0.1-SNAPSHOT.jar"
if [ ! -f "$PREBUILT_JAR" ]; then
    echo "Error: Pre-built JAR $PREBUILT_JAR not found"
    exit 1
fi
mkdir -p target
cp "$PREBUILT_JAR" "$TARGET_JAR"
if [ $? -ne 0 ]; then
    echo "Error: Failed to copy $PREBUILT_JAR to $TARGET_JAR"
    exit 1
fi

echo "Pre-built JAR copied to $TARGET_JAR"