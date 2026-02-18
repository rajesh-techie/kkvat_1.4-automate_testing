#!/usr/bin/env powershell
# Phase 4 Testing Script - Comprehensive API Test Suite

$API_BASE = "http://localhost:8080/api"
$TEST_RESULTS = @()

# Colors for output
$GREEN = "Green"
$RED = "Red"
$YELLOW = "Yellow"
$CYAN = "Cyan"

Function Log-Test($name, $status, $message) {
    $statusColor = if ($status -eq "✓") { $GREEN } else { $RED }
    Write-Host "[$status] $name" -ForegroundColor $statusColor
    if ($message) {
        Write-Host "    $message" -ForegroundColor Gray
    }
    $TEST_RESULTS += @{ Test = $name; Status = $status; Details = $message }
}

Function Test-Endpoint($method, $url, $body = $null, $token = $null) {
    try {
        $headers = @{ "Content-Type" = "application/json" }
        if ($token) {
            $headers["Authorization"] = "Bearer $token"
        }
        
        $params = @{
            Uri = $url
            Method = $method
            Headers = $headers
            ErrorAction = "Stop"
        }
        
        if ($body) {
            $params["Body"] = $body | ConvertTo-Json
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; StatusCode = 200 }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        $message = $_.Exception.Message
        return @{ Success = $false; Error = $message; StatusCode = $statusCode }
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  KKVat Phase 4 - Complete Test Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Authentication
Write-Host "1. AUTHENTICATION TESTS" -ForegroundColor Cyan
Write-Host "------------------------"

$loginBody = @{
    username = "admin"
    password = "Admin@123456"
}

$loginResponse = Test-Endpoint "POST" "$API_BASE/auth/login" $loginBody
if ($loginResponse.Success) {
    Log-Test "Admin Login" "✓" "Successfully authenticated"
    $TOKEN = $loginResponse.Data.token
    Write-Host "    Token: $($TOKEN.Substring(0, 50))..." -ForegroundColor Gray
}
else {
    Log-Test "Admin Login" "✗" $loginResponse.Error
    exit 1
}

Write-Host ""
Write-Host "2. REPORT VIEWS API TESTS" -ForegroundColor Cyan
Write-Host "-------------------------"

# Test 2a: Get all report views
$viewsResponse = Test-Endpoint "GET" "$API_BASE/report-views" $null $TOKEN
if ($viewsResponse.Success -and $viewsResponse.Data.Count -gt 0) {
    Log-Test "Get All Report Views" "✓" "Found $($viewsResponse.Data.Count) views"
    $FIRST_VIEW_ID = $viewsResponse.Data[0].id
    Write-Host "    Views: $(($viewsResponse.Data | ForEach-Object { $_.displayName }) -join ', ')" -ForegroundColor Gray
}
else {
    Log-Test "Get All Report Views" "✗" $viewsResponse.Error
}

# Test 2b: Get view by ID
if ($FIRST_VIEW_ID) {
    $viewByIdResponse = Test-Endpoint "GET" "$API_BASE/report-views/$FIRST_VIEW_ID" $null $TOKEN
    if ($viewByIdResponse.Success) {
        Log-Test "Get View by ID" "✓" "Retrieved view: $($viewByIdResponse.Data.displayName)"
    }
    else {
        Log-Test "Get View by ID" "✗" $viewByIdResponse.Error
    }
    
    # Test 2c: Get view fields
    $fieldsResponse = Test-Endpoint "GET" "$API_BASE/report-views/$FIRST_VIEW_ID/fields" $null $TOKEN
    if ($fieldsResponse.Success) {
        Log-Test "Get View Fields" "✓" "Found $($fieldsResponse.Data.Count) fields"
        Write-Host "    Fields: $(($fieldsResponse.Data | ForEach-Object { $_.displayName } | Select-Object -First 5) -join ', ')..." -ForegroundColor Gray
    }
    else {
        Log-Test "Get View Fields" "✗" $fieldsResponse.Error
    }
}

Write-Host ""
Write-Host "3. REPORT CRUD TESTS" -ForegroundColor Cyan
Write-Host "--------------------"

# Test 3a: Create a report
$reportBody = @{
    name = "Test Report Phase 4 - $(Get-Date -Format 'HH:mm:ss')"
    description = "Automated test report for Phase 4 validation"
    viewId = $FIRST_VIEW_ID
    selectedColumns = @("id", "status")
    reportType = "EXECUTION"
    isPublic = $false
}

$createResponse = Test-Endpoint "POST" "$API_BASE/reports" $reportBody $TOKEN
if ($createResponse.Success) {
    Log-Test "Create Report" "✓" "Created report: $($createResponse.Data.name)"
    $REPORT_ID = $createResponse.Data.id
    Write-Host "    Report ID: $REPORT_ID" -ForegroundColor Gray
}
else {
    Log-Test "Create Report" "✗" $createResponse.Error
    $REPORT_ID = $null
}

# Test 3b: Get all reports
$allReportsResponse = Test-Endpoint "GET" "$API_BASE/reports?page=0&size=10" $null $TOKEN
if ($allReportsResponse.Success) {
    Log-Test "Get All Reports" "✓" "Found $($allReportsResponse.Data.totalElements) reports"
}
else {
    Log-Test "Get All Reports" "✗" $allReportsResponse.Error
}

# Test 3c: Get report by ID
if ($REPORT_ID) {
    $getReportResponse = Test-Endpoint "GET" "$API_BASE/reports/$REPORT_ID" $null $TOKEN
    if ($getReportResponse.Success) {
        Log-Test "Get Report by ID" "✓" "Retrieved report: $($getReportResponse.Data.name)"
    }
    else {
        Log-Test "Get Report by ID" "✗" $getReportResponse.Error
    }
    
    # Test 3d: Update report
    $updateBody = @{
        name = "Updated Test Report - $(Get-Date -Format 'HH:mm:ss')"
        description = "Updated description for Phase 4 test"
        viewId = $FIRST_VIEW_ID
        selectedColumns = @("id", "status", "duration_ms")
        reportType = "EXECUTION"
        isPublic = $false
    }
    
    $updateResponse = Test-Endpoint "PUT" "$API_BASE/reports/$REPORT_ID" $updateBody $TOKEN
    if ($updateResponse.Success) {
        Log-Test "Update Report" "✓" "Updated report to: $($updateResponse.Data.name)"
    }
    else {
        Log-Test "Update Report" "✗" $updateResponse.Error
    }
}

Write-Host ""
Write-Host "4. REPORT SCHEDULING TESTS" -ForegroundColor Cyan
Write-Host "---------------------------"

# Test 4a: Create schedule
if ($REPORT_ID) {
    $scheduleBody = @{
        reportId = $REPORT_ID
        scheduleName = "Daily Test Report - $(Get-Date -Format 'HH:mm:ss')"
        frequency = "DAILY"
        timeOfDay = "09:00:00"
        emailRecipients = "test@example.com"
        isActive = $true
    }
    
    $createScheduleResponse = Test-Endpoint "POST" "$API_BASE/report-schedules" $scheduleBody $TOKEN
    if ($createScheduleResponse.Success) {
        Log-Test "Create Schedule" "✓" "Created schedule: $($createScheduleResponse.Data.scheduleName)"
        $SCHEDULE_ID = $createScheduleResponse.Data.id
        Write-Host "    Schedule ID: $SCHEDULE_ID" -ForegroundColor Gray
        Write-Host "    Next Execution: $($createScheduleResponse.Data.nextExecution)" -ForegroundColor Gray
    }
    else {
        Log-Test "Create Schedule" "✗" $createScheduleResponse.Error
        $SCHEDULE_ID = $null
    }
    
    # Test 4b: Get all schedules
    $allSchedulesResponse = Test-Endpoint "GET" "$API_BASE/report-schedules?page=0&size=10" $null $TOKEN
    if ($allSchedulesResponse.Success) {
        Log-Test "Get All Schedules" "✓" "Found $($allSchedulesResponse.Data.totalElements) schedules"
    }
    else {
        Log-Test "Get All Schedules" "✗" $allSchedulesResponse.Error
    }
    
    # Test 4c: Get schedule by ID
    if ($SCHEDULE_ID) {
        $getScheduleResponse = Test-Endpoint "GET" "$API_BASE/report-schedules/$SCHEDULE_ID" $null $TOKEN
        if ($getScheduleResponse.Success) {
            Log-Test "Get Schedule by ID" "✓" "Retrieved schedule: $($getScheduleResponse.Data.scheduleName)"
        }
        else {
            Log-Test "Get Schedule by ID" "✗" $getScheduleResponse.Error
        }
    }
}

Write-Host ""
Write-Host "5. REPORT EXECUTION TESTS" -ForegroundColor Cyan
Write-Host "-------------------------"

# Test 5a: Generate report manually
if ($REPORT_ID) {
    $generateResponse = Test-Endpoint "POST" "$API_BASE/report-executions/generate/$REPORT_ID" $null $TOKEN
    if ($generateResponse.Success) {
        Log-Test "Generate Report (Manual)" "✓" "Triggered generation, ID: $($generateResponse.Data.id)"
        $EXECUTION_ID = $generateResponse.Data.id
        Write-Host "    Status: $($generateResponse.Data.status)" -ForegroundColor Gray
    }
    else {
        Log-Test "Generate Report (Manual)" "✗" $generateResponse.Error
        $EXECUTION_ID = $null
    }
}

# Test 5b: Wait for execution to complete
if ($EXECUTION_ID) {
    Write-Host ""
    Write-Host "Waiting for report generation..." -ForegroundColor Yellow
    $testCount = 0
    $maxWait = 12  # 60 seconds max
    
    while ($testCount -lt $maxWait) {
        Start-Sleep -Seconds 5
        $execResponse = Test-Endpoint "GET" "$API_BASE/report-executions/$EXECUTION_ID" $null $TOKEN
        
        if ($execResponse.Success) {
            $status = $execResponse.Data.status
            Write-Host "Attempt $($testCount + 1)/$maxWait - Status: $status" -ForegroundColor Yellow
            
            if ($status -eq "COMPLETED") {
                Log-Test "Report Generation Complete" "✓" "Rows: $($execResponse.Data.rowCount), Size: $($execResponse.Data.fileSize) bytes"
                $EXECUTION_ID = $execResponse.Data.id
                break
            }
            elseif ($status -eq "FAILED") {
                Log-Test "Report Generation Failed" "✗" $execResponse.Data.errorMessage
                break
            }
        }
        $testCount++
    }
}

# Test 5c: Get execution history
if ($REPORT_ID) {
    $historyResponse = Test-Endpoint "GET" "$API_BASE/report-executions/report/$REPORT_ID?page=0&size=5" $null $TOKEN
    if ($historyResponse.Success) {
        Log-Test "Get Execution History" "✓" "Found $($historyResponse.Data.totalElements) executions"
    }
    else {
        Log-Test "Get Execution History" "✗" $historyResponse.Error
    }
}

Write-Host ""
Write-Host "6. SUMMARY" -ForegroundColor Cyan
Write-Host "----------"

$passed = ($TEST_RESULTS | Where-Object { $_.Status -eq "✓" }).Count
$failed = ($TEST_RESULTS | Where-Object { $_.Status -eq "✗" }).Count
$total = $TEST_RESULTS.Count

Write-Host "Total Tests: $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red

if ($failed -eq 0) {
    Write-Host ""
    Write-Host "✓ ALL TESTS PASSED - Phase 4 Backend is Working!" -ForegroundColor Green
}
else {
    Write-Host ""
    Write-Host "✗ Some tests failed - Review errors above" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
