# Life Vault - Digital Asset Information Management

A secure web application that helps users store asset information and automatically share it with trusted contacts during extended periods of inactivity.

## Features

- **Asset Management**: Store descriptions of important assets (bank accounts, investments, insurance, etc.) without sensitive account numbers
- **Trusted Contacts**: Designate people who should have access to your information
- **Inactivity Detection**: Automatic monitoring with customizable periods (default 6 months)
- **Secure Encryption**: All sensitive notes are encrypted at rest
- **Email Notifications**: Automated warnings and vault access notifications
- **JWT Authentication**: Secure token-based authentication
- **RESTful APIs**: Clean API design for easy integration

## Tech Stack

### Backend
- **Spring Boot 3.2** - Java framework
- **PostgreSQL** - Database
- **Spring Security** - Authentication & authorization
- **JWT** - Token-based auth
- **Spring Data JPA** - ORM
- **Quartz Scheduler** - Job scheduling
- **AWS SNS** - SMS notifications (optional)
- **Docker** - Containerization

### Frontend
- **React 18** - UI framework
- **TypeScript** - Type safety
- **Material-UI** - Component library
- **React Router** - Navigation
- **React Query** - Data fetching
- **React Hook Form** - Form handling
- **Vite** - Build tool

## Local Development

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### Backend Setup

1. Clone the repository
```bash
git clone [repository-url]
cd life-vault/life-vault-backend
```

2. Create PostgreSQL database
```sql
CREATE DATABASE lifevault;
```

3. Configure environment variables
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

4. Run the application
```bash
./mvnw spring-boot:run
```

The backend will start on http://localhost:8080

### Frontend Setup

1. Navigate to frontend directory
```bash
cd ../life-vault-frontend
```

2. Install dependencies
```bash
npm install
```

3. Start development server
```bash
npm run dev
```

The frontend will start on http://localhost:3000

## Docker Deployment

### Using Docker Compose

1. Build and run all services
```bash
cd life-vault-backend
docker-compose up --build
```

This will start:
- PostgreSQL on port 5432
- Backend API on port 8080
- You'll need to run the frontend separately

### Building Docker Images

Backend:
```bash
cd life-vault-backend
docker build -t life-vault-backend .
```

Frontend:
```bash
cd life-vault-frontend
docker build -t life-vault-frontend .
```

## AWS Deployment

### Prerequisites
- AWS Account
- AWS CLI configured
- ECR repository created
- ECS cluster set up
- RDS PostgreSQL instance

### 1. Push to ECR

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin [your-ecr-uri]

# Tag and push backend
docker tag life-vault-backend:latest [your-ecr-uri]/life-vault-backend:latest
docker push [your-ecr-uri]/life-vault-backend:latest
```

### 2. Create ECS Task Definition

Create a task definition with:
- Container image from ECR
- Environment variables for database connection
- Port mapping for 8080
- Health check endpoint: /actuator/health

### 3. Create ECS Service

- Use Fargate launch type
- Configure ALB for load balancing
- Set up auto-scaling policies
- Configure CloudWatch logs

### 4. Database Setup

- Use RDS PostgreSQL
- Enable encryption at rest
- Configure security groups
- Set up automated backups

### 5. Environment Variables

Set these in ECS task definition:
```
DB_HOST=your-rds-endpoint
DB_PORT=5432
DB_NAME=lifevault
DB_USERNAME=postgres
DB_PASSWORD=secure-password
JWT_SECRET=your-jwt-secret
ENCRYPTION_KEY=your-encryption-key
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-password
SNS_ENABLED=true
AWS_REGION=us-east-1
```

### 6. Frontend Deployment

Deploy React app to S3 + CloudFront:

```bash
cd life-vault-frontend
npm run build
aws s3 sync dist/ s3://your-bucket-name --delete
aws cloudfront create-invalidation --distribution-id YOUR_DIST_ID --paths "/*"
```

## Security Considerations

1. **Environment Variables**: Never commit sensitive data
2. **HTTPS**: Always use SSL in production
3. **CORS**: Configure allowed origins properly
4. **JWT Secret**: Use strong, random secrets
5. **Database**: Enable encryption at rest
6. **API Rate Limiting**: Implement rate limiting
7. **Input Validation**: All inputs are validated
8. **SQL Injection**: Using parameterized queries

## API Documentation

The API documentation is available at:
- Local: http://localhost:8080/swagger-ui.html
- Production: https://your-domain/swagger-ui.html

## Monitoring

- Health check: `/actuator/health`
- CloudWatch logs for application monitoring
- RDS performance insights
- ECS container insights

## Backup & Recovery

1. **Database Backups**: Automated daily RDS snapshots
2. **Point-in-time Recovery**: Enabled for RDS
3. **Multi-AZ Deployment**: For high availability

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.