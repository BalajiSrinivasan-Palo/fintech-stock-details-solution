#!/bin/bash

# Check if .env file exists
if [ ! -f backend/.env ]; then
    echo "Error: .env file not found!"
    echo "Please create backend/.env file with required environment variables."
    exit 1
fi

# Export environment variables
export $(cat backend/.env | xargs)

# Start services
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 30

# Run database migrations
docker-compose exec portfolio-service ./mvnw flyway:migrate

echo "All services are up and running!"
echo "Access the application at http://localhost:8080"
echo "Access Kibana at http://localhost:5601" 