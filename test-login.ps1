# Test Login Script
Write-Host "Waiting for backend to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "`nTesting login with username: admin, password: admin" -ForegroundColor Cyan

$loginBody = @{
    username = "admin"
    password = "admin"
} | ConvertTo-Json

try {
    Write-Host "Sending login request..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    Write-Host "`n✓ LOGIN SUCCESSFUL!" -ForegroundColor Green
    Write-Host "Username: $($response.user.username)" -ForegroundColor White
    Write-Host "Email: $($response.user.email)" -ForegroundColor White
    Write-Host "Role: $($response.user.role)" -ForegroundColor White
    Write-Host "Token: $($response.accessToken.Substring(0, 50))..." -ForegroundColor Yellow
    
} catch {
    Write-Host "`n✗ LOGIN FAILED!" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorDetail = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "Error: $($errorDetail.message)" -ForegroundColor Red
    } else {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}
