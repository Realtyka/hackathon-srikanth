#!/bin/bash

echo "🚀 Starting Life Vault Demo with Docker..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "📦 Building and starting all services..."
echo "This may take a few minutes on first run..."
echo ""

# Start all services
docker-compose -f docker-compose-local.yml up --build -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are running
if docker-compose -f docker-compose-local.yml ps | grep -q "Up"; then
    echo ""
    echo "✅ Life Vault is running!"
    echo ""
    echo "🌐 Access the application at:"
    echo "   Frontend: http://localhost:3000"
    echo "   Backend API: http://localhost:8080"
    echo "   API Docs: http://localhost:8080/swagger-ui.html"
    echo ""
    echo "📝 Demo Credentials:"
    echo "   Create a new account to get started!"
    echo ""
    echo "🛑 To stop all services, run:"
    echo "   ./stop-demo.sh"
    echo ""
    echo "📊 To view logs, run:"
    echo "   docker-compose -f docker-compose-local.yml logs -f"
else
    echo "❌ Failed to start services. Check the logs with:"
    echo "   docker-compose -f docker-compose-local.yml logs"
    exit 1
fi