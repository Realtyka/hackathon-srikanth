# Life Vault - Render Quick Start Guide

## Quick Deployment Steps

### 1. Push to GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin YOUR_GITHUB_URL
git push -u origin main
```

### 2. Backend Service (Web Service)
1. New+ → Web Service
2. Connect GitHub repo
3. Settings:
   - Name: `life-vault-backend`
   - Root Directory: `life-vault-backend`
   - Build: `./mvnw clean install -DskipTests`
   - Start: `java -Dspring.profiles.active=render -jar target/life-vault-backend-1.0.0.jar`

4. Environment Variables:
```
DB_HOST=(from your Postgres)
DB_PORT=5432
DB_NAME=(from your Postgres)
DB_USERNAME=(from your Postgres)
DB_PASSWORD=(from your Postgres)
SPRING_PROFILES_ACTIVE=render
JWT_SECRET=(generate with: openssl rand -base64 64)
ENCRYPTION_KEY=(generate with: openssl rand -base64 32 | head -c 32)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=(Gmail app password)
CORS_ORIGINS=https://life-vault-frontend.onrender.com
```

### 3. Frontend Service (Static Site)
1. New+ → Static Site
2. Connect GitHub repo
3. Settings:
   - Name: `life-vault-frontend`
   - Root Directory: `life-vault-frontend`
   - Build: `npm install && npm run build`
   - Publish: `dist`

4. Environment Variable:
```
VITE_API_URL=https://life-vault-backend.onrender.com/api
```

### 4. Final Steps
1. Wait for both services to deploy
2. Update backend CORS_ORIGINS with actual frontend URL
3. Test at: https://life-vault-frontend.onrender.com

### Gmail Setup
1. Enable 2FA on Gmail
2. Go to: https://myaccount.google.com/apppasswords
3. Create app password for "Mail"
4. Use this password for MAIL_PASSWORD

That's it! 🎉