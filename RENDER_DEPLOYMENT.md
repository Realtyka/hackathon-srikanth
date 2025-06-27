# Life Vault - Render Deployment Guide

This guide will walk you through deploying the Life Vault application to Render with separate backend and frontend services.

## Prerequisites
- Render account
- PostgreSQL database on Render (which you already have)
- GitHub repository with your code

## Step 1: Prepare Your Code

1. First, push your code to GitHub:
```bash
cd /Users/srikanthpulicherla/hackathon/life-vault
git init
git add .
git commit -m "Initial commit for Life Vault application"
git branch -M main
git remote add origin YOUR_GITHUB_REPO_URL
git push -u origin main
```

## Step 2: Set Up PostgreSQL Database

Since you already have a PostgreSQL database on Render, note down these values from your database dashboard:
- Internal Database URL (starts with `postgres://`)
- Or individual values:
  - Hostname
  - Port
  - Database name
  - Username
  - Password

## Step 3: Deploy Backend Service

### 3.1 Create Backend Web Service

1. Go to your Render dashboard
2. Click "New +" → "Web Service"
3. Connect your GitHub repository
4. Configure the service:
   - **Name**: `life-vault-backend`
   - **Root Directory**: `life-vault-backend`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean install -DskipTests`
   - **Start Command**: `java -Dspring.profiles.active=render -jar target/life-vault-backend-1.0.0.jar`
   - **Plan**: Free (or your preferred plan)

### 3.2 Set Environment Variables

Add these environment variables in the Render dashboard:

```bash
# Database Configuration (from your PostgreSQL database)
DB_HOST=your-database-hostname
DB_PORT=5432
DB_NAME=your-database-name
DB_USERNAME=your-database-username
DB_PASSWORD=your-database-password

# Spring Profile
SPRING_PROFILES_ACTIVE=render

# JWT Secret (generate a secure random string)
JWT_SECRET=your-very-long-random-string-at-least-512-bits

# Encryption Key (generate a secure 32-character string)
ENCRYPTION_KEY=your-32-character-encryption-key

# Email Configuration (for Gmail)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password

# CORS Configuration (update after frontend deployment)
CORS_ORIGINS=https://your-frontend-service.onrender.com

# Optional: AWS SNS for SMS (if you want SMS notifications)
SNS_ENABLED=false
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_REGION=us-east-1
```

#### Generating Secure Keys:

For JWT_SECRET (use one of these methods):
```bash
# macOS/Linux
openssl rand -base64 64

# Or using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

For ENCRYPTION_KEY (exactly 32 characters):
```bash
# macOS/Linux
openssl rand -base64 32 | head -c 32

# Or using Node.js
node -e "console.log(require('crypto').randomBytes(16).toString('hex'))"
```

## Step 4: Deploy Frontend Service

### 4.1 Create Frontend Static Site

1. Go to your Render dashboard
2. Click "New +" → "Static Site"
3. Connect your GitHub repository
4. Configure the service:
   - **Name**: `life-vault-frontend`
   - **Root Directory**: `life-vault-frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
   - **Plan**: Free (or your preferred plan)

### 4.2 Set Frontend Environment Variables

Add this environment variable:
```bash
VITE_API_URL=https://life-vault-backend.onrender.com/api
```

### 4.3 Update Backend CORS

After the frontend is deployed, go back to your backend service and update:
```bash
CORS_ORIGINS=https://life-vault-frontend.onrender.com
```

## Step 5: Configure Email Service

### For Gmail:
1. Enable 2-factor authentication on your Gmail account
2. Generate an app-specific password:
   - Go to https://myaccount.google.com/apppasswords
   - Create a new app password for "Mail"
   - Use this password for `MAIL_PASSWORD`

### Alternative Email Services:
- SendGrid
- Mailgun
- Amazon SES

## Step 6: Post-Deployment Steps

### 6.1 Verify Database Tables
The application will automatically create tables on first run due to `ddl-auto: update`.

### 6.2 Test the Application
1. Visit your frontend URL: `https://life-vault-frontend.onrender.com`
2. Create a new account
3. Test all features:
   - Add assets
   - Add trusted contacts
   - Update settings
   - Verify email notifications

### 6.3 Monitor Logs
- Check backend logs in Render dashboard
- Look for any database connection issues
- Verify scheduled jobs are running

## Step 7: Custom Domain (Optional)

### For Frontend:
1. Go to your frontend service settings
2. Add your custom domain
3. Follow Render's DNS configuration instructions

### For Backend (if needed):
1. Go to your backend service settings
2. Add a custom domain for API
3. Update `VITE_API_URL` in frontend to use the custom domain

## Troubleshooting

### Database Connection Issues:
- Verify database credentials
- Check if SSL is required (already configured in application-render.yml)
- Ensure database is in the same region as your services

### CORS Issues:
- Make sure `CORS_ORIGINS` matches your frontend URL exactly
- Include the protocol (https://)
- Don't include trailing slashes

### Build Failures:
- Check Java version (requires Java 17)
- Verify Maven wrapper permissions
- Check build logs for specific errors

### Email Not Sending:
- Verify email credentials
- Check if less secure app access is enabled (for Gmail)
- Consider using app-specific passwords

## Health Checks

Render will automatically monitor your services. The backend has a health endpoint at:
```
https://life-vault-backend.onrender.com/actuator/health
```

## Scaling Considerations

### Free Tier Limitations:
- Services spin down after 15 minutes of inactivity
- First request after spin-down will be slow
- Limited CPU and memory

### For Production:
- Upgrade to paid plans for always-on services
- Consider adding Redis for session management
- Enable auto-scaling for high traffic

## Security Checklist

- [ ] Strong JWT_SECRET (at least 512 bits)
- [ ] Strong ENCRYPTION_KEY (exactly 32 characters)
- [ ] Database uses SSL connection
- [ ] HTTPS enabled (automatic on Render)
- [ ] Environment variables properly set
- [ ] No secrets in code repository
- [ ] Email credentials are app-specific passwords

## Maintenance

### Database Backups:
- Render PostgreSQL includes daily backups
- Consider setting up additional backup strategies

### Updates:
- Use GitHub integration for automatic deployments
- Test updates in a staging environment first

### Monitoring:
- Set up alerts for service failures
- Monitor database performance
- Track scheduled job execution

## Support

If you encounter issues:
1. Check Render service logs
2. Verify all environment variables are set
3. Ensure database is accessible
4. Check GitHub integration status

Remember to never commit sensitive information like passwords or API keys to your repository!