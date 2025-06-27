# Life Vault Demo Guide

## 🚀 Quick Start

1. **Start the application**
   ```bash
   ./start-demo.sh
   ```

2. **Open your browser**
   - Frontend: http://localhost:3000
   - API Docs: http://localhost:8080/swagger-ui.html

3. **Stop the application**
   ```bash
   ./stop-demo.sh
   ```

## 📱 Demo Walkthrough

### 1. Create an Account
- Navigate to http://localhost:3000
- Click "Sign Up"
- Fill in the registration form:
  - First Name: John
  - Last Name: Doe
  - Email: john.doe@example.com
  - Phone: +1234567890 (optional)
  - Password: password123

### 2. Login
- Use your email and password to login
- You'll be redirected to the dashboard

### 3. Add Assets
- Click on "Assets" in the sidebar
- Click "Add Asset" button
- Try adding different types of assets:

**Example 1 - Bank Account:**
- Name: Chase Checking Account
- Type: Bank Account
- Institution: Chase Bank
- Location: New York, NY
- Description: Primary checking account
- Private Notes: Account ending in 1234

**Example 2 - Life Insurance:**
- Name: Term Life Insurance
- Type: Insurance
- Institution: MetLife
- Description: 20-year term policy, $500k coverage
- Private Notes: Policy #LI-789456

**Example 3 - Cryptocurrency:**
- Name: Bitcoin Wallet
- Type: Cryptocurrency
- Institution: Coinbase
- Description: Main crypto holdings
- Private Notes: Hardware wallet backup in safe

### 4. Add Trusted Contacts
- Click on "Trusted Contacts" in the sidebar
- Add family members or friends who should have access
- They'll receive verification emails (disabled in demo)

### 5. View Dashboard
- See summary of your assets
- Check your inactivity period (default 6 months)
- Review how the system works

## 🔑 Key Features to Demo

1. **Security**
   - JWT authentication (check browser DevTools > Application > Local Storage)
   - Encrypted private notes (stored encrypted in database)
   - Secure password hashing

2. **Asset Management**
   - Multiple asset types
   - No sensitive data required (no account numbers)
   - Edit and delete functionality

3. **Inactivity Detection**
   - Automated checking system
   - Email notifications (would be sent in production)
   - Configurable time periods

## 🛠️ Technical Demo Points

### View API Documentation
- Open http://localhost:8080/swagger-ui.html
- Shows all available endpoints
- Can test API calls directly

### Check Database
```bash
# Connect to PostgreSQL container
docker exec -it life-vault-postgres-1 psql -U postgres -d lifevault

# View tables
\dt

# Check users table
SELECT id, email, first_name, last_name FROM users;

# Check assets (note encrypted_notes)
SELECT id, name, type, encrypted_notes FROM assets;

# Exit
\q
```

### View Logs
```bash
# All services
docker-compose -f docker-compose-local.yml logs -f

# Just backend
docker-compose -f docker-compose-local.yml logs -f backend

# Just database
docker-compose -f docker-compose-local.yml logs -f postgres
```

## 🎯 Demo Scenarios

### Scenario 1: Estate Planning
"John wants to ensure his family can access important financial information if something happens to him."
- Add various assets (bank, insurance, investments)
- Add spouse and adult children as trusted contacts
- Explain 6-month inactivity trigger

### Scenario 2: Digital Asset Management
"Sarah has multiple crypto wallets and online accounts she wants to document."
- Add cryptocurrency assets
- Add digital platform accounts
- Show how private notes are encrypted

### Scenario 3: Business Owner
"Mike owns a business and wants partners to access critical info if needed."
- Add business-related assets
- Add business partners as contacts
- Discuss customizable inactivity periods

## 🔧 Troubleshooting

### Services won't start
```bash
# Check if ports are in use
lsof -i :3000
lsof -i :8080
lsof -i :5432

# Clean restart
docker-compose -f docker-compose-local.yml down -v
./start-demo.sh
```

### Can't access frontend
- Wait 30 seconds after starting for build to complete
- Check logs: `docker-compose -f docker-compose-local.yml logs frontend`

### Database connection issues
- Ensure PostgreSQL container is healthy
- Check credentials in docker-compose-local.yml

## 📊 Production Considerations

When discussing deployment:
1. **AWS Infrastructure**
   - ECS/Fargate for containers
   - RDS for PostgreSQL
   - S3 + CloudFront for frontend
   - SES for email, SNS for SMS

2. **Security Enhancements**
   - SSL/TLS certificates
   - AWS Secrets Manager
   - VPC and security groups
   - WAF for API protection

3. **Scalability**
   - Auto-scaling groups
   - Load balancers
   - Read replicas for database
   - CDN for static assets

4. **Monitoring**
   - CloudWatch logs and metrics
   - Application performance monitoring
   - Alerting for failed notifications
   - Audit trails for compliance