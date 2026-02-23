# Test EntityManagement Generator - Steps 1-8
$API_BASE = "http://localhost:8080/api"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Testing EntityManagement Generator" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Authentication
Write-Host ""
Write-Host "Step 1: Authenticating..." -ForegroundColor Cyan
$loginBody = @{
    username = "admin"
    password = "Admin@123456"
} | ConvertTo-Json

try {
    $loginResp = Invoke-RestMethod -Uri "$API_BASE/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $TOKEN = $loginResp.token
    Write-Host "✓ Login successful" -ForegroundColor Green
    Write-Host "  Token: $($TOKEN.Substring(0, 50))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Login failed: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Create EntityManagement record
Write-Host ""
Write-Host "Step 2: Creating EntityManagement record with TBL_USERS payload..." -ForegroundColor Cyan
$headers = @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
}

$payloadPath = "backend/tmp/TBL_USERS_payload.json"
if (Test-Path $payloadPath) {
    $payload = Get-Content $payloadPath -Raw
    try {
        $entityResp = Invoke-RestMethod -Uri "$API_BASE/entity-management" -Method POST -Headers $headers -Body $payload
        $ENTITY_ID = $entityResp.id
        Write-Host "✓ EntityManagement record created successfully" -ForegroundColor Green
        Write-Host "  Entity ID: $ENTITY_ID, Name: $($entityResp.entityName)" -ForegroundColor Gray
    } catch {
        Write-Host "✗ Failed to create EntityManagement record: $_" -ForegroundColor Red
        try {
            $errorDetail = $_.ErrorDetails.Message | ConvertFrom-Json
            Write-Host "  Error details: $($errorDetail.message)" -ForegroundColor Red
        } catch {}
        exit 1
    }
} else {
    Write-Host "✗ Payload file not found: $payloadPath" -ForegroundColor Red
    exit 1
}

# Step 3: Call generator (steps 1-8)
Write-Host ""
Write-Host "Step 3: Calling generator for steps 1-8..." -ForegroundColor Cyan
try {
    $generatorResp = Invoke-RestMethod -Uri "$API_BASE/entity-management/$ENTITY_ID/generate" -Method POST -Headers $headers
    Write-Host "✓ Generator executed successfully" -ForegroundColor Green
    Write-Host "  Result: $generatorResp" -ForegroundColor Gray
} catch {
    Write-Host "✗ Generator failed: $_" -ForegroundColor Red
    try {
        $errorDetail = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "  Error details: $($errorDetail.message)" -ForegroundColor Red
    } catch {}
}

# Step 4: Check generated files
Write-Host ""
Write-Host "Step 4: Checking generated files..." -ForegroundColor Cyan
$generatedPath = "generated/TBL_USERS"
if (Test-Path $generatedPath) {
    $files = Get-ChildItem $generatedPath -Name
    Write-Host "✓ Generated folder exists" -ForegroundColor Green
    Write-Host "  Files: $($files -join ', ')" -ForegroundColor Gray
    
    # Check progress.json
    $progressPath = "$generatedPath/progress.json"
    if (Test-Path $progressPath) {
        $progress = Get-Content $progressPath -Raw | ConvertFrom-Json
        Write-Host "✓ Progress file exists with $($progress.Count) steps" -ForegroundColor Green
        foreach ($step in $progress) {
            $status = if ($step.status -eq "SUCCESS") { "✓" } elseif ($step.status -eq "FAILED") { "✗" } else { "⚠" }
            Write-Host "    $status Step $($step.step): $($step.name) [$($step.status)]" -ForegroundColor $(if ($step.status -eq "SUCCESS") { "Green" } elseif ($step.status -eq "FAILED") { "Red" } else { "Yellow" })
        }
    }
} else {
    Write-Host "✗ Generated folder not found: $generatedPath" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan