# Simple Database Setup for KKVat

## Option 1: Using MySQL Workbench (Easiest)

1. **Open MySQL Workbench**
2. **Connect to your MySQL instance**
3. **Open the schema.sql file**: 
   - File → Open SQL Script
   - Navigate to: `d:\python_programs_rajesh\kkvat_1.4-automate_testing\database\schema.sql`
4. **Execute the script**: Click the lightning bolt icon
5. **Done!** The database is created

## Option 2: Using PowerShell with Full Path

Run PowerShell as **Administrator** and execute:

```powershell
# Start MySQL service
Start-Service MySQL96

# Set MySQL path
$mysqlPath = "C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysql.exe"

# Navigate to database folder
cd d:\python_programs_rajesh\kkvat_1.4-automate_testing\database

# Run the schema (replace YOUR_PASSWORD with your MySQL root password)
Get-Content schema.sql | & $mysqlPath -u root -pYOUR_PASSWORD

# Verify
"SHOW DATABASES;" | & $mysqlPath -u root -pYOUR_PASSWORD
```

## Option 3: Manual SQL Commands

If the above don't work, run these commands in MySQL Workbench or command line:

```sql
-- 1. Create database
CREATE DATABASE IF NOT EXISTS kkvat_automation 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE kkvat_automation;

-- 2. Then paste the entire content from schema.sql file
```

## After Database is Created

Update the backend configuration:

**File**: `backend\src\main\resources\application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kkvat_automation?useSSL=false&serverTimezone=UTC
    username: root  # or kkvat_user if you created it
    password: YOUR_MYSQL_PASSWORD
```

## Verify Database Setup

In MySQL Workbench, run:

```sql
USE kkvat_automation;
SHOW TABLES;
SELECT * FROM users;
```

You should see 8 tables and 1 admin user.

## Next Step

After database is ready, we'll start the backend!
