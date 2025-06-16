#!/bin/bash

# EmpVerify Blockchain API Startup Script (Gradle Version)

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_message() {
    echo -e "${BLUE}[EmpVerify API]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Default values
ENVIRONMENT="local"
FABRIC_BASE_PATH=""
API_PORT="8080"
BUILD_MODE="jar"
DEBUG_MODE="false"

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -e, --environment ENV    Environment (local|ec2|docker) [default: local]"
    echo "  -p, --port PORT         API port [default: 8080]"
    echo "  -f, --fabric-path PATH  Custom Fabric network path"
    echo "  -m, --mode MODE         Build mode (jar|run) [default: jar]"
    echo "  -d, --debug             Enable debug mode (port 5005)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Build modes:"
    echo "  jar                     Build JAR and run (production-like)"
    echo "  run                     Use gradle bootRun (development)"
    echo ""
    echo "Examples:"
    echo "  $0                              # Build JAR and run locally"
    echo "  $0 -e ec2                       # Build JAR and run on EC2"
    echo "  $0 -m run                       # Use gradle bootRun for development"
    echo "  $0 -m run -d                    # Development mode with debugging"
    echo "  $0 -e local -p 8081 -m jar     # Custom port with JAR build"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -p|--port)
            API_PORT="$2"
            shift 2
            ;;
        -f|--fabric-path)
            FABRIC_BASE_PATH="$2"
            shift 2
            ;;
        -m|--mode)
            BUILD_MODE="$2"
            shift 2
            ;;
        -d|--debug)
            DEBUG_MODE="true"
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate environment
if [[ "$ENVIRONMENT" != "local" && "$ENVIRONMENT" != "ec2" && "$ENVIRONMENT" != "docker" ]]; then
    print_error "Invalid environment: $ENVIRONMENT. Must be 'local', 'ec2', or 'docker'"
    exit 1
fi

# Validate build mode
if [[ "$BUILD_MODE" != "jar" && "$BUILD_MODE" != "run" ]]; then
    print_error "Invalid build mode: $BUILD_MODE. Must be 'jar' or 'run'"
    exit 1
fi

print_message "Starting EmpVerify Blockchain API (Gradle)..."
print_message "Environment: $ENVIRONMENT"
print_message "Build Mode: $BUILD_MODE"
print_message "Port: $API_PORT"
print_message "Debug Mode: $DEBUG_MODE"

# Set Fabric base path based on environment if not provided
if [[ -z "$FABRIC_BASE_PATH" ]]; then
    case "$ENVIRONMENT" in
        "local")
            FABRIC_BASE_PATH="/Users/mac/Desktop/fabric-samples/test-network"
            ;;
        "ec2")
            FABRIC_BASE_PATH="/opt/empverify/fabric-samples/test-network"
            ;;
        "docker")
            FABRIC_BASE_PATH="/fabric-network"
            ;;
    esac
fi

print_message "Fabric Network Path: $FABRIC_BASE_PATH"

# Check if Fabric network path exists (skip for docker)
if [[ "$ENVIRONMENT" != "docker" ]]; then
    if [[ ! -d "$FABRIC_BASE_PATH" ]]; then
        print_error "Fabric network path does not exist: $FABRIC_BASE_PATH"
        print_message "Please ensure the Hyperledger Fabric test network is set up at the specified path"
        exit 1
    fi

    # Check for required Fabric network files
    REQUIRED_FILES=(
        "organizations/peerOrganizations/org1.agregartech.com/connection-org1.yaml"
        "organizations/peerOrganizations/org1.agregartech.com/users/User1@org1.example.com/msp/signcerts/cert.pem"
        "organizations/peerOrganizations/org1.agregartech.com/users/User1@org1.example.com/msp/keystore"
    )

    for file in "${REQUIRED_FILES[@]}"; do
        if [[ ! -e "$FABRIC_BASE_PATH/$file" ]]; then
            print_error "Required Fabric file not found: $FABRIC_BASE_PATH/$file"
            print_message "Please ensure the Fabric network is properly set up and certificates are generated"
            exit 1
        fi
    done

    print_success "Fabric network files validation passed"
fi

# Check if Gradle wrapper exists
if [[ -f "./gradlew" ]]; then
    GRADLE_CMD="./gradlew"
elif command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
else
    print_error "Gradle not found. Please install Gradle or use Gradle wrapper"
    exit 1
fi

print_message "Using Gradle: $GRADLE_CMD"

# Set environment variables
export SERVER_PORT="$API_PORT"
export FABRIC_NETWORK_BASE_PATH="$FABRIC_BASE_PATH"
export SPRING_PROFILES_ACTIVE="$ENVIRONMENT"

# Create logs directory if it doesn't exist
mkdir -p logs

print_message "Building the application..."

# Clean and build
if ! $GRADLE_CMD clean build -x test; then
    print_error "Failed to build the application"
    exit 1
fi

print_success "Application built successfully"

# Function to handle cleanup on script exit
cleanup() {
    print_message "Shutting down..."
    if [[ -n "$API_PID" ]]; then
        kill $API_PID 2>/dev/null || true
    fi
}

trap cleanup EXIT

# Start the application based on build mode
if [[ "$BUILD_MODE" == "jar" ]]; then
    # JAR mode - find and run the JAR file
    JAR_FILE=$(find build/libs -name "empverify-blockchain-api*.jar" | head -n 1)

    if [[ -z "$JAR_FILE" ]]; then
        print_error "JAR file not found in build/libs directory"
        exit 1
    fi

    print_message "Starting application: $JAR_FILE"

    # Set JVM arguments
    JVM_ARGS="-Xmx512m -Xms256m"
    JVM_ARGS="$JVM_ARGS -Dspring.profiles.active=$ENVIRONMENT"
    JVM_ARGS="$JVM_ARGS -Dserver.port=$API_PORT"
    JVM_ARGS="$JVM_ARGS -Dfabric.network.base-path=$FABRIC_BASE_PATH"

    if [[ "$DEBUG_MODE" == "true" ]]; then
        JVM_ARGS="$JVM_ARGS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
        print_message "Debug mode enabled - connect debugger to port 5005"
    fi

    # Start the JAR
    java $JVM_ARGS -jar "$JAR_FILE" 2>&1 | tee logs/api-$(date +%Y%m%d-%H%M%S).log &
    API_PID=$!

elif [[ "$BUILD_MODE" == "run" ]]; then
    # Gradle bootRun mode
    print_message "Starting application with gradle bootRun"

    # Set Gradle properties
    GRADLE_ARGS=""
    GRADLE_ARGS="$GRADLE_ARGS -Dspring.profiles.active=$ENVIRONMENT"
    GRADLE_ARGS="$GRADLE_ARGS -Dserver.port=$API_PORT"
    GRADLE_ARGS="$GRADLE_ARGS -Dfabric.network.base-path=$FABRIC_BASE_PATH"

    if [[ "$DEBUG_MODE" == "true" ]]; then
        GRADLE_ARGS="$GRADLE_ARGS -Pdebug"
        print_message "Debug mode enabled - connect debugger to port 5005"
    fi

    # Use the appropriate run task based on environment
    case "$ENVIRONMENT" in
        "local")
            RUN_TASK="runLocal"
            ;;
        "ec2")
            RUN_TASK="runEc2"
            ;;
        "docker")
            RUN_TASK="runDocker"
            ;;
        *)
            RUN_TASK="bootRun"
            ;;
    esac

    # Start with gradle bootRun
    $GRADLE_CMD $RUN_TASK $GRADLE_ARGS 2>&1 | tee logs/api-$(date +%Y%m%d-%H%M%S).log &
    API_PID=$!
fi

print_success "API started with PID: $API_PID"
print_message "API will be available at: http://localhost:$API_PORT/api/v1"
print_message "Swagger UI: http://localhost:$API_PORT/api/v1/swagger-ui.html"
print_message "Health Check: http://localhost:$API_PORT/api/v1/employment-records/health"

if [[ "$DEBUG_MODE" == "true" ]]; then
    print_message "Debug port: 5005"
fi

print_message "Press Ctrl+C to stop the application"

# Wait for the application to start
sleep 5

# Health check
print_message "Performing health check..."
if curl -s "http://localhost:$API_PORT/api/v1/employment-records/health" > /dev/null; then
    print_success "Health check passed - API is running"
else
    print_warning "Health check failed - API might still be starting up"
fi

# Wait for the process to finish
wait $API_PID