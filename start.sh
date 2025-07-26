#!/bin/bash
# Set JAVA_HOME to Render's JDK or bundled JDK
export JAVA_HOME=${JAVA_HOME:-/opt/render/jdk-21}
if [ -d "$(pwd)/jdk-21.0.2" ]; then
    export JAVA_HOME=$(pwd)/jdk-21.0.2
fi
export PATH=$JAVA_HOME/bin:$PATH


# Check if Java is available
if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java not found in $JAVA_HOME/bin"
    exit 1
fi
if ! command -v javac >/dev/null 2>&1; then
    echo "Error: javac not found in $JAVA_HOME/bin. JAVA_HOME must point to a JDK, not a JRE"
    exit 1
fi

# Verify required environment variables
if [ -z "$JWT_SECRET" ]; then
    echo "Error: JWT_SECRET not set"
    exit 1
fi
if [ -z "$PORT" ]; then
    echo "Error: PORT not set"
    exit 1
fi
if [ -z "$SPRING_DATASOURCE_URL" ] || [ -z "$SPRING_DATASOURCE_USERNAME" ] || [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
    echo "Error: Database environment variables (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD) not set"
    exit 1
fi

# Check if JAR file exists
JAR_FILE="artifacts/e-learning-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file $JAR_FILE not found"
    exit 1
fi

# Log environment setup
echo "JAVA_HOME set to $JAVA_HOME"
echo "Java version:"
java -version
echo "Starting application on port $PORT with JAR $JAR_FILE"

# Run the application
java -jar $JAR_FILE --server.port=$PORT