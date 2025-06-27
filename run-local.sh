#!/bin/bash

echo "Starting Life Vault Application..."

# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=lifevault
export DB_USERNAME=postgres
export DB_PASSWORD=password
export JWT_SECRET=local-development-secret-key-change-in-production
export ENCRYPTION_KEY=local-encryption-key-change-in-production
export CORS_ORIGINS=http://localhost:3000

# Optional: Disable email for local testing
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=test@example.com
export MAIL_PASSWORD=test

echo "Starting Backend..."
cd life-vault-backend
./mvnw spring-boot:run &
BACKEND_PID=$!

echo "Waiting for backend to start..."
sleep 10

echo "Starting Frontend..."
cd ../life-vault-frontend
npm install
npm run dev &
FRONTEND_PID=$!

echo ""
echo "======================================"
echo "Life Vault is starting up!"
echo "======================================"
echo "Backend API: http://localhost:8080"
echo "Frontend UI: http://localhost:3000"
echo "API Docs: http://localhost:8080/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop all services"
echo "======================================"

# Wait for user to press Ctrl+C
trap "kill $BACKEND_PID $FRONTEND_PID; exit" INT
wait