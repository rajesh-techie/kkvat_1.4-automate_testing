# KKVat Automation Platform v1.4

## 🎯 Overview

A comprehensive test automation platform with record and replay capabilities, designed for government use with NIST 800-53 and CIS Benchmarks compliance.

### Key Features

- **🎬 Record & Replay**: Capture user actions and replay them as automated tests
- **👥 User Management**: Complete user and group management with RBAC
- **🔐 Enterprise Security**: JWT authentication, password policies, audit logging
- **📊 Dynamic Reporting**: Generate custom reports on test executions
- **🏛️ Government Ready**: NIST/CIS compliant security controls
- **🎭 Playwright Integration**: Cross-browser test automation support

---

## 🏗️ Architecture

### Technology Stack

**Backend:**
- Spring Boot 3.2.2
- Java 21
- MySQL 8.x
- Spring Security + JWT
- Playwright 1.41.0

**Frontend:**
- Angular 17
- TypeScript 5.x
- Angular Material
- NgRx (State Management)
- RxJS

**Security:**
- BCrypt (strength 12)
- JWT tokens (15-min expiration)
- Role-based access control
- Comprehensive audit logging

---

## 📋 Prerequisites

### Required Software

1. **Java Development Kit 21+**
   - Download: https://adoptium.net/

2. **Maven 3.8+**
   - Download: https://maven.apache.org/download.cgi

3. **MySQL 8.x**
   - Download: https://dev.mysql.com/downloads/

4. **Node.js 18+**
   - Download: https://nodejs.org/

5. **Angular CLI 17**
   ```bash
   npm install -g @angular/cli@17
   ```

---

## 🚀 Quick Start

### 1. Database Setup

```powershell
cd database
.\setup-database.ps1
```

Follow the prompts to:
- Create the `kkvat_automation` database
- Create tables and schema
- Set up the application user
- Insert default admin account

### 2. Backend Setup

```powershell
cd backend

# Update application.yml with your database password
# Edit: src/main/resources/application.yml

# Install Playwright browsers (first time only)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"

# Run the backend
mvn spring-boot:run
```

Backend will start at: **http://localhost:8080/api**

Swagger UI: **http://localhost:8080/api/swagger-ui.html**

### 3. Frontend Setup

```powershell
cd frontend
.\setup-frontend.ps1

# After setup completes
cd kkvat-frontend
ng serve
```

Frontend will start at: **http://localhost:4200**

---

## 🔑 Default Credentials

**Initial Login:**
- Username: `admin`
- Password: `Admin@123456`

⚠️ **CRITICAL**: Change this password immediately after first login!

---

## 📁 Project Structure

```
kkvat_1.4-automate_testing/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/kkvat/automation/
│   │   │   │   ├── config/          # Configuration
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── dto/             # Data transfer objects
│   │   │   │   ├── exception/       # Exception handling
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── repository/      # Data access
│   │   │   │   ├── security/        # Security components
│   │   │   │   └── service/         # Business logic
│   │   │   └── resources/
│   │   │       └── application.yml  # App configuration
│   │   └── test/
│   ├── pom.xml              # Maven dependencies
│   └── README.md            # Backend documentation
│
├── frontend/                # Angular frontend
│   ├── kkvat-frontend/     # Angular app (created by setup)
│   ├── setup-frontend.ps1  # Setup script
│   └── SETUP.md            # Frontend documentation
│
├── database/               # Database files
│   ├── schema.sql         # MySQL schema
│   └── setup-database.ps1 # Database setup script
│
├── PROGRESS.md            # Development progress
└── README.md              # This file
```

---

## 🔐 Security Features

### NIST 800-53 Compliance

✅ **Access Control (AC)**
- Role-based access control (RBAC)
- Least privilege principle
- Session management

✅ **Identification & Authentication (IA)**
- Multi-factor authentication ready
- Password complexity requirements
- Account lockout after failed attempts
- Password expiration (90 days)

✅ **Audit & Accountability (AU)**
- Comprehensive audit logging
- User action tracking
- Timestamp tracking
- Immutable audit records

✅ **System & Communications Protection (SC)**
- TLS 1.3 for data in transit
- Encrypted database connections
- Secure session tokens

### Password Policy

- Minimum 12 characters
- Uppercase letters required
- Lowercase letters required
- Numbers required
- Special characters required
- Expires after 90 days
- Account locks after 5 failed attempts

### Session Management

- 15-minute idle timeout
- Single concurrent session enforcement
- Secure JWT tokens
- Automatic session cleanup

---

## 📊 User Roles

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access, user management, audit logs |
| **TEST_MANAGER** | Manage users, groups, test cases, reports |
| **TESTER** | Create/execute tests, view results |
| **VIEWER** | View-only access to tests and reports |

---

## 🎬 Recording Test Cases

### How It Works

1. **Start Recording**: Click "Record New Test" in the application
2. **Perform Actions**: Navigate and interact with the target web application
3. **Playwright Captures**: All clicks, inputs, and navigation events
4. **Save Test**: Actions stored as JSON in the database
5. **Replay Anytime**: Execute the recorded test case

### Action Types Captured

- `navigate` - URL navigation
- `click` - Mouse clicks
- `type` - Keyboard inputs
- `assert` - Page element assertions

### JSON Format Example

```json
{
  "testCaseId": "TC-001",
  "name": "Login Test",
  "steps": [
    {"action": "navigate", "url": "https://example.com"},
    {"action": "type", "selector": "#username", "value": "user1"},
    {"action": "type", "selector": "#password", "value": "****"},
    {"action": "click", "selector": "#submit"},
    {"action": "assert", "type": "visible", "selector": "#dashboard"}
  ]
}
```

---

## 🔧 Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kkvat_automation
    username: kkvat_user
    password: ${DB_PASSWORD}

app:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 900000  # 15 minutes
```

### Environment Variables

Set these for production:

```powershell
# Windows PowerShell
$env:DB_PASSWORD="YourSecurePassword"
$env:JWT_SECRET="YourSecretKeyMinimum256Bits"
```

### Frontend Configuration

Edit `frontend/kkvat-frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## 🧪 Testing

### Backend Tests

```bash
cd backend
mvn test
```

### Frontend Tests

```bash
cd frontend/kkvat-frontend
ng test
```

---

## 📦 Building for Production

### Backend

```bash
cd backend
mvn clean package -DskipTests
```

Artifact: `target/automation-platform-1.4.0.jar`

### Frontend

```bash
cd frontend/kkvat-frontend
ng build --configuration production
```

Output: `dist/kkvat-frontend/`

---

## 🚢 Deployment

### On-Premise Deployment

1. **Database**: Set up MySQL on your server
2. **Backend**: Deploy JAR file
   ```bash
   java -jar automation-platform-1.4.0.jar
   ```
3. **Frontend**: Serve Angular build with nginx/Apache
4. **Reverse Proxy**: Configure for HTTPS

### System Requirements

- **CPU**: 2+ cores recommended
- **RAM**: 4GB minimum, 8GB recommended
- **Disk**: 20GB minimum
- **Concurrent Users**: Tested for 10 users

---

## 📖 API Documentation

Access Swagger UI when backend is running:
- URL: http://localhost:8080/api/swagger-ui.html

### Main Endpoints

**Authentication:**
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

**Users:**
- `GET /api/users` - List users
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

**Test Cases:**
- `GET /api/test-cases` - List test cases
- `POST /api/test-cases` - Create test case
- `POST /api/test-cases/{id}/execute` - Execute test
- `GET /api/test-cases/{id}/executions` - Execution history

---

## 🐛 Troubleshooting

### Common Issues

**1. Database Connection Failed**
```
Solution: Verify MySQL is running and credentials are correct
mysql -u kkvat_user -p
```

**2. Port 8080 Already in Use**
```
Solution: Change port in application.yml or stop conflicting service
```

**3. JWT Token Errors**
```
Solution: Ensure JWT_SECRET environment variable is set
```

**4. Playwright Installation Failed**
```
Solution: Run with admin privileges:
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

---

## 📈 Development Roadmap

### Phase 1: Foundation ✅ (COMPLETED)
- Backend infrastructure
- Security implementation
- Database schema
- Authentication system

### Phase 2: User Management (Next)
- User CRUD operations
- Group management
- Password reset functionality
- User profile management

### Phase 3: Test Automation
- Recording interface
- Test case management
- Execution engine
- Results visualization

### Phase 4: Reporting
- Dynamic report builder
- Export capabilities (PDF, Excel)
- Scheduled reports
- Dashboard analytics

### Phase 5: Advanced Features
- CI/CD integration
- Scheduled test execution
- Email notifications
- Test data management

---

## 🤝 Support

For issues or questions:
1. Check the documentation in each module
2. Review troubleshooting section
3. Check audit logs for errors

---

## 📄 License

Internal use only - Government project

---

## ✅ Current Status

**Phase 1 Completed:**
- ✅ Backend Spring Boot application
- ✅ MySQL database schema
- ✅ JWT security implementation
- ✅ Authentication endpoints
- ✅ Audit logging framework
- ✅ Exception handling
- ✅ API documentation (Swagger)
- ✅ Setup scripts for database and frontend

**Next Steps:**
1. Run database setup script
2. Run backend application
3. Run frontend setup script
4. Test authentication flow
5. Begin Phase 2 implementation

---

**Built with ❤️ for secure, reliable test automation**
