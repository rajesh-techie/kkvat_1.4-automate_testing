# KKVat Automation Platform - Backend

## Spring Boot 3.2.2 + Java 21 + MySQL

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- MySQL 8.x
- Node.js (for Playwright browsers)

### Setup Instructions

#### 1. Database Setup

```bash
# Login to MySQL as root
mysql -u root -p

# Run the schema script
mysql -u root -p < ../database/schema.sql

# Create database user (if not already done in schema.sql)
CREATE USER 'kkvat_user'@'localhost' IDENTIFIED BY 'YourSecurePassword@123';
GRANT SELECT, INSERT, UPDATE, DELETE ON kkvat_automation.* TO 'kkvat_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 2. Configure Application

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    password: YourActualDatabasePassword
```

Set environment variables for security:
```bash
# Windows PowerShell
$env:DB_PASSWORD="YourDatabasePassword"
$env:JWT_SECRET="your-secret-key-at-least-256-bits-long-for-production-use"

# Linux/Mac
export DB_PASSWORD="YourDatabasePassword"
export JWT_SECRET="your-secret-key-at-least-256-bits-long-for-production-use"
```

#### 3. Install Playwright Browsers

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

#### 4. Build the Application

```bash
mvn clean install
```

#### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### API Documentation

Once running, access Swagger UI:
- **URL**: http://localhost:8080/api/swagger-ui.html

### Default Credentials

- **Username**: admin
- **Password**: Admin@123456

**⚠️ IMPORTANT**: Change the default password immediately after first login!

### API Endpoints

#### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout

#### Users (Admin/Test Manager only)
- `GET /api/users` - List all users
- `POST /api/users` - Create user
- `GET /api/users/{id}` - Get user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

#### Groups (Admin/Test Manager only)
- `GET /api/groups` - List all groups
- `POST /api/groups` - Create group
- `GET /api/groups/{id}` - Get group
- `PUT /api/groups/{id}` - Update group
- `DELETE /api/groups/{id}` - Delete group

#### Test Cases (All authenticated)
- `GET /api/test-cases` - List test cases
- `POST /api/test-cases` - Create test case
- `GET /api/test-cases/{id}` - Get test case
- `PUT /api/test-cases/{id}` - Update test case
- `DELETE /api/test-cases/{id}` - Delete test case
- `POST /api/test-cases/{id}/execute` - Execute test

#### Audit Logs (Admin only)
- `GET /api/audit` - View audit logs

### Security Features

✅ **NIST 800-53 Compliant**
- JWT authentication (15-minute expiration)
- BCrypt password hashing (strength 12)
- Password complexity requirements
- Account lockout (5 failed attempts)
- Password expiry (90 days)
- Session management
- Comprehensive audit logging

✅ **CIS Benchmarks**
- Principle of least privilege
- Secure session handling
- Input validation
- SQL injection prevention
- XSS protection

### Project Structure

```
src/main/java/com/kkvat/automation/
├── config/              # Application configuration
├── controller/          # REST controllers
├── dto/                 # Data transfer objects
├── exception/           # Exception handling
├── model/               # JPA entities
├── repository/          # Data access layer
├── security/            # Security components
└── service/             # Business logic

src/main/resources/
└── application.yml      # Application properties
```

### Troubleshooting

#### Database Connection Issues
- Verify MySQL is running: `mysql -u kkvat_user -p`
- Check credentials in application.yml
- Ensure database exists: `USE kkvat_automation;`

#### Port Already in Use
Change port in application.yml:
```yaml
server:
  port: 8081
```

#### JWT Token Errors
- Ensure JWT_SECRET is set and is at least 256 bits
- Check token expiration time

### Development

#### Running Tests
```bash
mvn test
```

#### Building for Production
```bash
mvn clean package -DskipTests
java -jar target/automation-platform-1.4.0.jar
```

### Monitoring

Health check endpoint:
- `GET /api/actuator/health`

### Support

For issues or questions, refer to the main project documentation.
