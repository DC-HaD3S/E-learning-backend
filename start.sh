#!/bin/bash
# Set JAVA_HOME
export JAVA_HOME=${JAVA_HOME:-$(pwd)/jdk-21.0.2}
export PATH=$JAVA_HOME/bin:$PATH

# Check Java
if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java not found in $JAVA_HOME/bin"
    exit 1
fi
if ! command -v javac >/dev/null 2>&1; then
    echo "Error: javac not found in $JAVA_HOME/bin. JAVA_HOME must point to a JDK, not a JRE"
    exit 1
fi

# Verify environment variables
if [ -z "$JWT_SECRET" ]; then
    echo "Error: JWT_SECRET not set"
    exit 1
fi
if [ -z "$PORT" ]; then
    echo "Error: PORT not set"
    exit 1
fi
if [ -z "$SPRING_DATASOURCE_URL" ] || [ -z "$SPRING_DATASOURCE_USERNAME" ] || [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
    echo "Error: Database environment variables not set"
    exit 1
fi

# Check JAR
JAR_FILE="target/e-learning-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file $JAR_FILE not found"
    exit 1
fi

# Log setup
echo "JAVA_HOME set to $JAVA_HOME"
echo "Java version:"
java -version
echo "Starting application on port $PORT with JAR $JAR_FILE"

# Run application
java -jar $JAR_FILE --server.port=$PORT