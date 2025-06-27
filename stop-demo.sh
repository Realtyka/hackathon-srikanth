#!/bin/bash

echo "🛑 Stopping Life Vault Demo..."
docker-compose -f docker-compose-local.yml down

echo "✅ All services stopped."
echo ""
echo "To remove all data and start fresh, run:"
echo "  docker-compose -f docker-compose-local.yml down -v"