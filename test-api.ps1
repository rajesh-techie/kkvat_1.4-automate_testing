# Test Login API

Write-Host "Testing KKVat Authentication API..." -ForegroundColor Cyan
Write-Host ""

# Wait for server to be ready
Write-Host "Waiting for backend to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Test 1: Health Check
Write-Host "1. Testing Health Endpoint..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/actuator/health" -Method GET
    Write-Host "   ✓ Health check passed: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Login with Admin
Write-Host "2. Testing Login API..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "Admin@123456"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody
    
    Write-Host "   ✓ Login successful!" -ForegroundColor Green
    Write-Host "   User: $($response.user.username)" -ForegroundColor White
    Write-Host "   Role: $($response.user.role)" -ForegroundColor White
    Write-Host "   Token: $($response.accessToken.Substring(0, 20))..." -ForegroundColor White
    Write-Host ""
    
    # Save token for next test
    $token = $response.accessToken
    
    # Test 3: Logout
    Write-Host "3. Testing Logout API..." -ForegroundColor Yellow
    try {
        $logoutResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/logout" `
            -Method POST `
            -Headers @{ "Authorization" = "Bearer $token" }
        
        Write-Host "   ✓ Logout successful!" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ Logout failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "   ✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "   Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

Write-Host ""
Write-Host "===================================" -ForegroundColor Green
Write-Host "All tests passed!" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Green
Write-Host ""
Write-Host "Backend is ready at: http://localhost:8080/api" -ForegroundColor Cyan
Write-Host "Swagger UI: http://localhost:8080/api/swagger-ui.html" -ForegroundColor Cyan
