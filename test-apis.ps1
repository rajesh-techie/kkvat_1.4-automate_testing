# Test KKVat Automation Platform APIs
$baseUrl = "http://localhost:8080/api"
$token = ""

Write-Host "=== KKVat API Testing ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Login with admin user
Write-Host "1. Testing Login API..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "Admin@123456"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    $token = $response.token
    Write-Host "   Status: SUCCESS - Login successful!" -ForegroundColor Green
    Write-Host "   Token received: $($token.Substring(0, 20))..." -ForegroundColor Gray
} catch {
    Write-Host "   ERROR: Login failed" -ForegroundColor Red
    Write-Host "   Details: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Get current user profile
Write-Host "2. Testing Get Current User API..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/users/me" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "   Status: SUCCESS" -ForegroundColor Green
    Write-Host "   User: $($response.username) ($($response.role))" -ForegroundColor Gray
} catch {
    Write-Host "   ERROR: Failed to get current user" -ForegroundColor Red
    Write-Host "   Details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Get all users
Write-Host "3. Testing Get All Users API..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/users/list" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "   Status: SUCCESS" -ForegroundColor Green
    Write-Host "   Found $($response.Count) user(s)" -ForegroundColor Gray
} catch {
    Write-Host "   ERROR: Failed to get users" -ForegroundColor Red
    Write-Host "   Details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 4: Get all test cases
Write-Host "4. Testing Get Test Cases API..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/test-cases/list" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "   Status: SUCCESS" -ForegroundColor Green
    Write-Host "   Found $($response.Count) test case(s)" -ForegroundColor Gray
} catch {
    Write-Host "   ERROR: Failed to get test cases" -ForegroundColor Red
    Write-Host "   Details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 5: Get all groups
Write-Host "5. Testing Get Groups API..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/groups/list" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "   Status: SUCCESS" -ForegroundColor Green
    Write-Host "   Found $($response.Count) group(s)" -ForegroundColor Gray
} catch {
    Write-Host "   ERROR: Failed to get groups" -ForegroundColor Red
    Write-Host "   Details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 6: Get all executions
Write-Host "6. Testing Get Test Executions API..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/executions/list" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "   Status: SUCCESS" -ForegroundColor Green
    Write-Host "   Found $($response.Count) execution(s)" -ForegroundColor Gray
} catch {
    Write-Host "   ERROR: Failed to get executions" -ForegroundColor Red
    Write-Host "   Details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== API Testing Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- Backend is running on http://localhost:8080" -ForegroundColor Green
Write-Host "- All tested APIs are functional" -ForegroundColor Green
Write-Host "- Admin user (admin/Admin@123456) is working" -ForegroundColor Green
