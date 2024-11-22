#!/bin/bash

LOG_FILE="run-dev.log"

# Function to log messages with a timestamp
log() {
    echo "$(date +'%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log "Starting development environment..."

# Start Docker containers
log "Starting Docker containers..."
docker-compose up -d

# Check if Docker containers started successfully
if [ $? -ne 0 ]; then
    log "Failed to start Docker containers"
    exit 1
fi
log "Docker containers started successfully."


# Wait for MySQL to be healthy
log "Waiting for MySQL to be ready..."
for i in {1..30}; do
    if [ "$(docker inspect -f '{{.State.Health.Status}}' mysql_db)" == "healthy" ]; then
        log "MySQL is ready."
        break
    fi
    log "MySQL is not ready yet. Checking again in 2 seconds..."
    sleep 2
done

# Check if MySQL is still not healthy after 60 seconds
if [ "$(docker inspect -f '{{.State.Health.Status}}' mysql_db)" != "healthy" ]; then
    log "MySQL did not become healthy in time."
    docker-compose down; log 'Development environment stopped.'; exit 1
fi

# Start the Spring application in the background
log "Starting the Spring application..."
mvn spring-boot:run &

# Save the PID of the Spring application
SPRING_PID=$!

# Trap SIGINT and SIGTERM to shut down gracefully
trap "log 'Stopping...'; kill $SPRING_PID; docker-compose down; log 'Development environment stopped.'; exit 0;" SIGINT SIGTERM

# Wait for the Spring application to exit
wait $SPRING_PID

log "Spring application has exited."
