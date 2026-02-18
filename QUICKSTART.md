# 🚀 Quick Start Guide - KKVat Automation Platform

## Phase 1 is Complete! Let's Test It

Follow these steps to get the application running:

---

## Step 1: Database Setup (5 minutes)

Open PowerShell as Administrator and run:

```powershell
cd d:\python_programs_rajesh\kkvat_1.4-automate_testing\database
.\setup-database.ps1
```

**What you'll be asked:**
1. MySQL root username (default: root)
2. MySQL root password
3. Password for the application user `kkvat_user` (minimum 12 characters)

**Example:**
```
MySQL root username: root
MySQL root password: ********
Password for kkvat_user: SecurePass@123
```

✅ **Success Indicator**: You'll see "Database setup completed!" and a list of tables created.

---

## Step 2: Configure Backend (2 minutes)

1. Open file: `backend\src\main\resources\application.yml`

2. Update the database password:
```yaml
spring:
  datasource:
    password: SecurePass@123  # Use the password you set in Step 1
```

3. **Optional but Recommended**: Set environment variables for security:
```powershell
$env:DB_PASSWORD="SecurePass@123"
$env:JWT_SECRET="kkvat-jwt-secret-key-for-production-use-minimum-256-bits-long"
```

---

## Step 3: Start Backend (3 minutes)

Open PowerShell:

```powershell
cd d:\python_programs_rajesh\kkvat_1.4-automate_testing\backend

# First time only: Install Playwright browsers
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"

# Start the backend
mvn spring-boot:run
```

**Wait for this message:**
```
Started KkvatAutomationApplication in X seconds
```

✅ **Backend is running at**: http://localhost:8080/api

---

## Step 4: Test the Backend (2 minutes)

### Option A: Using Browser
Open: http://localhost:8080/api/swagger-ui.html

### Option B: Using PowerShell
```powershell
# Test login
$body = @{
    username = "admin"
    password = "Admin@123456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

✅ **If you see this, Phase 1 is working perfectly!**

---

## Step 5: Frontend Setup (Optional for Phase 1)

```powershell
cd d:\python_programs_rajesh\kkvat_1.4-automate_testing\frontend
.\setup-frontend.ps1
```

Follow the prompts. This will:
- Install Angular CLI
- Create the Angular project
- Install dependencies
- Set up project structure

After setup:
```powershell
cd kkvat-frontend
ng serve
```

Frontend at: http://localhost:4200

---

## 🎯 What You Can Test Now

### 1. Login API
```powershell
POST http://localhost:8080/api/auth/login
Body: {"username": "admin", "password": "Admin@123456"}
```

### 2. Check Health
```powershell
GET http://localhost:8080/api/actuator/health
```

### 3. View API Docs
Browser: http://localhost:8080/api/swagger-ui.html

### 4. Check Audit Logs (in database)
```sql
USE kkvat_automation;
SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 10;
```

---

## 🔍 Verification Checklist

- [ ] Database created with 8 tables
- [ ] Backend starts without errors
- [ ] Can login with admin/Admin@123456
- [ ] Receive JWT token in response
- [ ] Audit log created for login action
- [ ] Swagger UI accessible
- [ ] Health endpoint returns UP

---

## 🐛 Common Issues & Solutions

### Issue: "Access denied for user 'kkvat_user'"
**Solution**: Check password in application.yml matches what you set

### Issue: "Port 8080 already in use"
**Solution**: Change port in application.yml:
```yaml
server:
  port: 8081
```

### Issue: "Schema not found"
**Solution**: Run database setup script again

### Issue: "JWT Secret too short"
**Solution**: Use minimum 256-bit (32 character) secret

---

## 📊 Test Results You Should See

### In Console (Backend):
```
INFO  c.k.a.KkvatAutomationApplication - Started KkvatAutomationApplication
INFO  c.k.a.service.AuthService - User logged in successfully: admin
INFO  c.k.a.service.AuditService - Audit log created: admin - LOGIN - USER - SUCCESS
```

### In Database:
```sql
-- Check users
SELECT id, username, role, is_active, last_login FROM users;

-- Check audit logs
SELECT username, action, resource_type, status, timestamp 
FROM audit_logs 
ORDER BY timestamp DESC;

-- Check sessions
SELECT user_id, ip_address, is_active, created_at 
FROM sessions 
WHERE is_active = 1;
```

---

## ✅ Success!

If everything above works, **Phase 1 is successfully completed!**

### What We've Built:
✅ Complete backend API
✅ Secure authentication system
✅ JWT token management
✅ Password policies & account lockout
✅ Comprehensive audit logging
✅ Database schema with relationships
✅ Exception handling
✅ API documentation

### Next Steps:
1. Change default admin password
2. Create additional users
3. Proceed to Phase 2 (User & Group Management)
4. Build the frontend UI

---

## 🆘 Need Help?

Check these files:
- [Main README](../README.md)
- [Backend README](../backend/README.md)
- [PROGRESS.md](../PROGRESS.md)
- Database logs: `d:\python_programs_rajesh\kkvat\logs\`

---

**Ready to proceed to Phase 2? Let me know!** 🚀
