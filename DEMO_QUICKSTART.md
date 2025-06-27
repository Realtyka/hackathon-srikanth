# Life Vault Demo - Quick Start Guide

## 🚀 5-Minute Demo Setup

### 1. Enable Demo Mode (One-time setup)
```bash
# From the life-vault directory
./enable-demo-mode.sh

# Rebuild and restart with demo mode
docker-compose -f docker-compose-local.yml down
docker-compose -f docker-compose-local.yml build backend
docker-compose -f docker-compose-local.yml up -d
```

### 2. Access the Application
- Frontend: http://localhost:3000
- Backend: http://localhost:8080

### 3. Create Test Account
1. Click "Create Account"
2. Use a real email address to see notifications
3. Log in with your credentials

### 4. Quick Demo Flow

#### Option A: Use Demo Dashboard (Easiest!)
1. Navigate to **Demo** in the sidebar (only visible in demo mode)
2. Select a scenario from the dropdown:
   - **50% Warning**: First notification
   - **75% Warning**: More urgent
   - **Final Week**: Daily warnings
   - **Grace Period**: Every 2 days
   - **Expired**: Contacts notified
3. Click **Simulate** to backdate your activity
4. Click **Trigger Check Now** to send notifications
5. Check your email!

#### Option B: Quick Database Commands
```bash
# Connect to database
docker exec -it life-vault-postgres-1 psql -U postgres -d lifevault

# Simulate different stages (replace email)
UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '135 days' WHERE email = 'your@email.com';

# Exit psql
\q
```

## 📧 Email Notifications You'll See

### 50% Warning (45 days left)
- Subject: "Life Vault - Routine Check-In Required"
- One-click verification link
- Friendly reminder tone

### 75% Warning (22.5 days left)
- Subject: "Life Vault - Important: Activity Verification Needed"
- More urgent messaging
- Clear timeline

### Final Week (7 days left)
- Subject: "Life Vault - URGENT: Final Week Notice"
- Daily reminders
- Warning about contact notification

### Grace Period (Past deadline)
- Subject: "Life Vault - CRITICAL: Grace Period Active"
- Every 2 days
- Final chance before contacts notified

## 🎯 Key Demo Points

1. **Privacy**: Show that adding a trusted contact doesn't notify them
2. **One-Click Verification**: Click email link - no login required
3. **Progressive Warnings**: Increasing urgency over time
4. **Demo Dashboard**: Easy simulation without waiting months

## 🔧 Troubleshooting

### Demo Mode Notes:
1. **Emails are disabled in demo mode** - Check logs instead
2. View simulated notifications: `docker logs life-vault-backend-1 | grep DEMO`
3. Activity logs show what would happen in production

### Demo menu not visible?
- Only appears when running on localhost
- Refresh the page after login

### Need to reset?
```bash
docker-compose -f docker-compose-local.yml down -v
docker-compose -f docker-compose-local.yml up -d
```

## 📱 Mobile Demo
1. Open Chrome DevTools (F12)
2. Toggle device toolbar (Ctrl+Shift+M)
3. Select a mobile device
4. Show responsive navigation and forms