# Life Vault Demo Guide

> **Quick Start?** See [DEMO_QUICKSTART.md](./DEMO_QUICKSTART.md) for a 5-minute demo setup!

## Starting the Application

1. **Start the application:**
```bash
cd /Users/srikanthpulicherla/hackathon/life-vault
bash start-demo.sh
```

2. **Access the application:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

## Demo Flow

### 1. User Registration & Setup

1. **Create a new account:**
   - Go to http://localhost:3000
   - Click "Create Account"
   - Fill in details (use a real email to see notifications)
   - Sign up and log in

2. **Add Assets:**
   - Go to Assets page
   - Add several assets (bank accounts, investments, etc.)
   - Note: Only descriptions are stored, not sensitive numbers

3. **Add Trusted Contacts:**
   - Go to Trusted Contacts
   - Add 2-3 contacts with real email addresses
   - **Important**: Contacts are NOT notified when added (privacy feature)

4. **Configure Settings:**
   - Go to Settings
   - Set inactivity period (for demo, set to minimum 90 days)
   - Update profile information

### 2. Demonstrating the Notification System

Since waiting 90+ days isn't practical for a demo, here are ways to demonstrate the notification features:

#### Option A: Manual Database Update (Quickest for Demo)

1. **Connect to PostgreSQL:**
```bash
docker exec -it life-vault-postgres-1 psql -U postgres -d lifevault
```

2. **View current user:**
```sql
SELECT id, email, last_activity_date, inactivity_period_days FROM users;
```

3. **Simulate inactivity by backdating last_activity_date:**
```sql
-- For 50% warning (45 days before deadline)
UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '135 days' WHERE email = 'your-email@example.com';

-- For 75% warning (22.5 days before deadline) 
UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '157 days' WHERE email = 'your-email@example.com';

-- For final week warning
UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '173 days' WHERE email = 'your-email@example.com';

-- For grace period
UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '181 days' WHERE email = 'your-email@example.com';
```

4. **Trigger the scheduler manually:**
```bash
# Check backend logs to see scheduled job running
docker logs -f life-vault-backend-1
```

#### Option B: API Testing (Developer-Friendly)

1. **Create a test endpoint (temporary for demo):**

Add this to `UserController.java`:
```java
@PostMapping("/demo/trigger-notifications")
@PreAuthorize("hasRole('ADMIN')") // Or remove for demo
public ResponseEntity<String> triggerNotifications() {
    inactivityCheckScheduler.checkInactiveUsers();
    return ResponseEntity.ok("Notification check triggered");
}
```

2. **Call the endpoint:**
```bash
# Get auth token first
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email@example.com","password":"your-password"}' \
  | jq -r '.token')

# Trigger notifications
curl -X POST http://localhost:8080/api/users/demo/trigger-notifications \
  -H "Authorization: Bearer $TOKEN"
```

#### Option C: Scheduler Configuration (Best for Testing)

1. **Temporarily modify the scheduler to run frequently:**

Edit `InactivityCheckScheduler.java`:
```java
// Change from daily to every minute for demo
@Scheduled(fixedDelay = 60000) // 1 minute
public void checkInactiveUsers() {
    // existing code
}
```

2. **Restart the backend:**
```bash
docker-compose -f docker-compose-local.yml restart backend
```

### 3. Email Notification Demo

1. **Configure real email (if not done):**
   - Use Gmail with app-specific password
   - Or use a service like Mailtrap for testing

2. **What to expect in emails:**

   **50% Warning Email:**
   - Subject: "Life Vault - Routine Check-In Required"
   - Contains one-click verification link
   - Mentions 45 days remaining

   **75% Warning Email:**
   - Subject: "Life Vault - Important: Activity Verification Needed"
   - More urgent tone
   - Contains one-click verification link

   **Final Week Email:**
   - Subject: "Life Vault - URGENT: Final Week Notice"
   - Daily reminders
   - Clear warning about contact notification

   **Grace Period Email:**
   - Subject: "Life Vault - CRITICAL: Grace Period Active"
   - Sent every 2 days
   - Final warning before contacts are notified

### 4. One-Click Verification Demo

1. **From any warning email:**
   - Click the "Verify Activity" button
   - Should redirect to success page
   - No login required
   - Updates last_activity_date automatically

2. **Alternative verification:**
   - Simply log into the application
   - Any authenticated action updates activity

### 5. Contact Notification Demo

When grace period expires:

1. **Trusted contacts receive email:**
   - Subject: "Important: You've been designated as a trusted contact"
   - Explains the situation
   - Provides secure link to view assets
   - Includes vault owner's information

2. **Contact access portal:**
   - Special login for trusted contacts
   - View-only access to asset information
   - Cannot modify or delete anything

## Demo Script

### Quick 10-Minute Demo

1. **Setup (2 min):**
   - Create account
   - Add 2 assets
   - Add 1 trusted contact
   - Show privacy notice

2. **Notification System (5 min):**
   - Show notification schedule in Settings
   - Use database update to simulate inactivity
   - Show email received
   - Click verification link
   - Show activity updated

3. **Security Features (3 min):**
   - Show encrypted notes on assets
   - Show that contacts aren't notified when added
   - Show one-click verification (no login needed)
   - Explain grace period concept

## Troubleshooting

### Emails not sending:
1. Check email configuration in docker-compose.yml
2. Verify SMTP credentials
3. Check spam folder
4. View backend logs: `docker logs life-vault-backend-1`

### Scheduler not running:
1. Check if backend is healthy: `docker ps`
2. Look for "Scheduler started" in logs
3. Verify cron expression in code

### Database connection issues:
1. Ensure PostgreSQL is running: `docker ps`
2. Check connection string in logs
3. Verify environment variables

## Key Features to Highlight

1. **Privacy First:**
   - Contacts don't know they're designated
   - No sensitive financial data stored
   - Encrypted notes

2. **User Control:**
   - Configurable inactivity period
   - One-click activity verification
   - Can remove/add contacts anytime

3. **Progressive Notifications:**
   - Multiple warnings before action
   - Increasing urgency
   - Grace period as final safety

4. **Security:**
   - JWT authentication
   - Encrypted sensitive data
   - Secure one-time verification tokens

## Reset Demo Data

To start fresh:
```bash
# Stop containers
docker-compose -f docker-compose-local.yml down

# Remove volumes (clears database)
docker-compose -f docker-compose-local.yml down -v

# Restart
docker-compose -f docker-compose-local.yml up -d
```