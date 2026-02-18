# Phase 4 API Testing Script

$API_BASE = "http://localhost:8080/api"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  KKVat Phase 4 - API Testing Started" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Authentication
Write-Host ""
Write-Host "Step 1: Testing Authentication..." -ForegroundColor Cyan
$loginUrl = "$API_BASE/auth/login"
$loginBody = @{
    username = "admin"
    password = "Admin@123456"
} | ConvertTo-Json

try {
    $loginResp = Invoke-RestMethod -Uri $loginUrl -Method POST -Body $loginBody -ContentType "application/json"
    $TOKEN = $loginResp.token
    Write-Host "✓ Login successful" -ForegroundColor Green
    Write-Host "  Token: $($TOKEN.Substring(0, 50))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Login failed: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Get Report Views
Write-Host ""
Write-Host "Step 2: Testing Report Views API..." -ForegroundColor Cyan
$viewsUrl = "$API_BASE/report-views"
$headers = @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
}

try {
    $viewsResp = Invoke-RestMethod -Uri $viewsUrl -Method GET -Headers $headers
    Write-Host "✓ Retrieved report views: $($viewsResp.Count) views" -ForegroundColor Green
    $FIRST_VIEW_ID = $viewsResp[0].id
    Write-Host "  Views: $(($viewsResp | ForEach-Object { $_.displayName }) -join ', ')" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get report views: $_" -ForegroundColor Red
}

# Step 3: Create Report
Write-Host ""
Write-Host "Step 3: Testing Report CRUD (Create)..." -ForegroundColor Cyan
$reportBody = @{
    name = "Test Report $(Get-Date -Format 'HHmmss')"
    description = "Automated Phase 4 test"
    viewId = $FIRST_VIEW_ID
    selectedColumns = @("id", "status")
    reportType = "EXECUTION"
    isPublic = $false
} | ConvertTo-Json

try {
    $createResp = Invoke-RestMethod -Uri "$API_BASE/reports" -Method POST -Headers $headers -Body $reportBody
    $REPORT_ID = $createResp.id
    Write-Host "✓ Report created successfully" -ForegroundColor Green
    Write-Host "  Report ID: $REPORT_ID, Name: $($createResp.name)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to create report: $_" -ForegroundColor Red
}

# Step 4: Get Report by ID
Write-Host ""
Write-Host "Step 4: Testing Report CRUD (Read)..." -ForegroundColor Cyan
try {
    $getResp = Invoke-RestMethod -Uri "$API_BASE/reports/$REPORT_ID" -Method GET -Headers $headers
    Write-Host "✓ Report retrieved successfully" -ForegroundColor Green
    Write-Host "  Name: $($getResp.name), View: $($getResp.viewId)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get report: $_" -ForegroundColor Red
}

# Step 5: Update Report
Write-Host ""
Write-Host "Step 5: Testing Report CRUD (Update)..." -ForegroundColor Cyan
$updateBody = @{
    name = "Updated Test Report $(Get-Date -Format 'HHmmss')"
    description = "Updated via API test"
    viewId = $FIRST_VIEW_ID
    selectedColumns = @("id", "status", "duration_ms")
    reportType = "EXECUTION"
    isPublic = $false
} | ConvertTo-Json

try {
    $updateResp = Invoke-RestMethod -Uri "$API_BASE/reports/$REPORT_ID" -Method PUT -Headers $headers -Body $updateBody
    Write-Host "✓ Report updated successfully" -ForegroundColor Green
    Write-Host "  New name: $($updateResp.name)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to update report: $_" -ForegroundColor Red
}

# Step 6: Create Schedule
Write-Host ""
Write-Host "Step 6: Testing Report Scheduling (Create)..." -ForegroundColor Cyan
$scheduleBody = @{
    reportId = $REPORT_ID
    scheduleName = "Daily Test $(Get-Date -Format 'HHmmss')"
    frequency = "DAILY"
    timeOfDay = "09:00:00"
    emailRecipients = "test@example.com"
    isActive = $true
} | ConvertTo-Json

try {
    $schedResp = Invoke-RestMethod -Uri "$API_BASE/report-schedules" -Method POST -Headers $headers -Body $scheduleBody
    $SCHEDULE_ID = $schedResp.id
    Write-Host "✓ Schedule created successfully" -ForegroundColor Green
    Write-Host "  Schedule ID: $SCHEDULE_ID, Next Execution: $($schedResp.nextExecution)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to create schedule: $_" -ForegroundColor Red
}

# Step 7: Get Schedule
Write-Host ""
Write-Host "Step 7: Testing Report Scheduling (Read)..." -ForegroundColor Cyan
try {
    $getSchedResp = Invoke-RestMethod -Uri "$API_BASE/report-schedules/$SCHEDULE_ID" -Method GET -Headers $headers
    Write-Host "✓ Schedule retrieved successfully" -ForegroundColor Green
    Write-Host "  Name: $($getSchedResp.scheduleName), Frequency: $($getSchedResp.frequency)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get schedule: $_" -ForegroundColor Red
}

# Step 8: Generate Report
Write-Host ""
Write-Host "Step 8: Testing Report Execution (Generate)..." -ForegroundColor Cyan
try {
    $genResp = Invoke-RestMethod -Uri "$API_BASE/report-executions/generate/$REPORT_ID" -Method POST -Headers $headers
    $EXECUTION_ID = $genResp.id
    Write-Host "✓ Report generation triggered" -ForegroundColor Green
    Write-Host "  Execution ID: $EXECUTION_ID, Status: $($genResp.status)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to generate report: $_" -ForegroundColor Red
}

# Step 9: Monitor Execution
Write-Host ""
Write-Host "Step 9: Waiting for report generation to complete..." -ForegroundColor Yellow
$waited = 0
$maxWait = 60
while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 5
    $waited += 5
    
    try {
        $execResp = Invoke-RestMethod -Uri "$API_BASE/report-executions/$EXECUTION_ID" -Method GET -Headers $headers
        $status = $execResp.status
        Write-Host "  Status: $status (waited ${waited}s)" -ForegroundColor Yellow
        
        if ($status -eq "COMPLETED") {
            Write-Host "✓ Report generation completed" -ForegroundColor Green
            Write-Host "  Rows: $($execResp.rowCount), Size: $($execResp.fileSize) bytes" -ForegroundColor Gray
            break
        } elseif ($status -eq "FAILED") {
            Write-Host "✗ Report generation failed: $($execResp.errorMessage)" -ForegroundColor Red
            break
        }
    } catch {
        Write-Host "  Error checking status: $_" -ForegroundColor Red
    }
}

# Step 10: List Report History
Write-Host ""
Write-Host "Step 10: Testing Report Execution (History)..." -ForegroundColor Cyan
try {
    $histResp = Invoke-RestMethod -Uri "$API_BASE/report-executions/report/$REPORT_ID?page=0&size=5" -Method GET -Headers $headers
    Write-Host "✓ Report history retrieved" -ForegroundColor Green
    Write-Host "  Total executions: $($histResp.totalElements)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get execution history: $_" -ForegroundColor Red
}

# Step 11: Get All Reports (Pagination Test)
Write-Host ""
Write-Host "Step 11: Testing Report Listing (Pagination)..." -ForegroundColor Cyan
try {
    $listResp = Invoke-RestMethod -Uri "$API_BASE/reports?page=0&size=10" -Method GET -Headers $headers
    Write-Host "✓ Reports list retrieved" -ForegroundColor Green
    Write-Host "  Total reports: $($listResp.totalElements), Current page: $($listResp.numberOfElements)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to list reports: $_" -ForegroundColor Red
}

# Step 12: Delete Report
Write-Host ""
Write-Host "Step 12: Testing Report CRUD (Delete)..." -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$API_BASE/reports/$REPORT_ID" -Method DELETE -Headers $headers
    Write-Host "✓ Report deleted successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to delete report: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Phase 4 API Testing Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
