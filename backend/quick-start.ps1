# Quick start script for backend
Write-Host "Starting KKVat Backend..." -ForegroundColor Cyan

# Prompt for MySQL password
$securePassword = Read-Host "Enter MySQL root password" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Create temporary application-local.yml with the password
$config = @"
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kkvat_automation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: $password
"@

$config | Out-File -FilePath "application-local.yml" -Encoding UTF8

Write-Host "Starting backend server..." -ForegroundColor Green
java -jar target\automation-platform-1.4.0.jar --spring.profiles.active=local --spring.config.location=file:./application-local.yml
