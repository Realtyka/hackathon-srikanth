#!/bin/bash

echo "🚀 Preparing Life Vault for Render.com deployment..."
echo ""

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "📦 Initializing git repository..."
    git init
fi

# Add all files
echo "📄 Adding files to git..."
git add .

# Commit
echo "💾 Creating commit..."
git commit -m "Prepare for Render.com deployment" || echo "No changes to commit"

echo ""
echo "✅ Repository is ready for deployment!"
echo ""
echo "📋 Next steps:"
echo ""
echo "1. Create a GitHub repository:"
echo "   - Go to https://github.com/new"
echo "   - Name it 'life-vault'"
echo "   - Don't initialize with README"
echo ""
echo "2. Push your code:"
echo "   git remote add origin https://github.com/YOUR_USERNAME/life-vault.git"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "3. Deploy on Render.com:"
echo "   - Go to https://render.com"
echo "   - Follow instructions in RENDER_DEPLOYMENT.md"
echo ""
echo "📚 Documentation:"
echo "   - Deployment Guide: RENDER_DEPLOYMENT.md"
echo "   - Demo Guide: DEMO_GUIDE.md"
echo "   - README: README.md"