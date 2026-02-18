# KKVat Database Setup Script
# Run this script to set up the MySQL database

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "KKVat Database Setup" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# Check if MySQL is installed
Write-Host "Checking MySQL installation..." -ForegroundColor Yellow
$mysqlVersion = mysql --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: MySQL is not installed or not in PATH!" -ForegroundColor Red
    Write-Host "Please install MySQL 8.x from https://dev.mysql.com/downloads/" -ForegroundColor Red
    exit 1
}
Write-Host "MySQL found: $mysqlVersion" -ForegroundColor Green
Write-Host ""

# Get MySQL root credentials
Write-Host "Please enter MySQL root credentials:" -ForegroundColor Yellow
$rootUser = Read-Host "MySQL root username (default: root)"
if ([string]::IsNullOrWhiteSpace($rootUser)) {
    $rootUser = "root"
}

$rootPassword = Read-Host "MySQL root password" -AsSecureString
$rootPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($rootPassword)
)

Write-Host ""
Write-Host "Testing MySQL connection..." -ForegroundColor Yellow

# Test connection
$testConnection = "SELECT 1;" | mysql -u $rootUser -p$rootPasswordPlain 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to connect to MySQL!" -ForegroundColor Red
    Write-Host "Please check your credentials and try again." -ForegroundColor Red
    exit 1
}
Write-Host "Connection successful!" -ForegroundColor Green
Write-Host ""

# Run schema script
Write-Host "Creating database and tables..." -ForegroundColor Yellow
$schemaPath = Join-Path $PSScriptRoot "schema.sql"

if (-not (Test-Path $schemaPath)) {
    Write-Host "ERROR: schema.sql not found at $schemaPath" -ForegroundColor Red
    exit 1
}

Get-Content $schemaPath | mysql -u $rootUser -p$rootPasswordPlain 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to execute schema.sql!" -ForegroundColor Red
    exit 1
}

Write-Host "Database created successfully!" -ForegroundColor Green
Write-Host ""

# Create application user
Write-Host "Creating application database user..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Enter password for kkvat_user (min 12 chars):" -ForegroundColor Yellow
$appPassword = Read-Host "Password" -AsSecureString
$appPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($appPassword)
)

if ($appPasswordPlain.Length -lt 12) {
    Write-Host "WARNING: Password should be at least 12 characters!" -ForegroundColor Yellow
}

# Create user and grant privileges
$createUserSQL = @"
DROP USER IF EXISTS 'kkvat_user'@'localhost';
CREATE USER 'kkvat_user'@'localhost' IDENTIFIED BY '$appPasswordPlain';
GRANT SELECT, INSERT, UPDATE, DELETE ON kkvat_automation.* TO 'kkvat_user'@'localhost';
FLUSH PRIVILEGES;
"@

$createUserSQL | mysql -u $rootUser -p$rootPasswordPlain 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Database user created successfully!" -ForegroundColor Green
} else {
    Write-Host "WARNING: Failed to create database user. You may need to do this manually." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Verifying setup..." -ForegroundColor Yellow

# Verify tables
$tables = "USE kkvat_automation; SHOW TABLES;" | mysql -u $rootUser -p$rootPasswordPlain -s 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Tables created:" -ForegroundColor Green
    $tables | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
} else {
    Write-Host "WARNING: Could not verify tables" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "===================================" -ForegroundColor Green
Write-Host "Database setup completed!" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Green
Write-Host ""
Write-Host "Configuration for application.yml:" -ForegroundColor Cyan
Write-Host "  Database: kkvat_automation" -ForegroundColor White
Write-Host "  Username: kkvat_user" -ForegroundColor White
Write-Host "  Password: [the password you entered]" -ForegroundColor White
Write-Host ""
Write-Host "Default admin credentials:" -ForegroundColor Cyan
Write-Host "  Username: admin" -ForegroundColor White
Write-Host "  Password: Admin@123456" -ForegroundColor White
Write-Host ""
Write-Host "IMPORTANT: Change the admin password after first login!" -ForegroundColor Yellow
Write-Host ""
Write-Host "Next: Update backend/src/main/resources/application.yml with the database password" -ForegroundColor Yellow
