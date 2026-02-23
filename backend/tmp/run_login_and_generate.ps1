$ErrorActionPreference = 'Stop'
$loginFile = 'tmp/login.json'
$payloadFile = 'tmp/TBL_USERS_payload.json'
$loginRespFile = 'tmp/login_response.json'
if (-Not (Test-Path $loginFile)) { Write-Host "Login file not found: $loginFile"; exit 1 }
if (-Not (Test-Path $payloadFile)) { Write-Host "Payload file not found: $payloadFile"; exit 1 }
$loginBody = Get-Content -Raw $loginFile
try {
    Write-Host "Logging in..."
    $loginResp = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Headers @{ 'Content-Type'='application/json' } -Body $loginBody -TimeoutSec 30
    $loginResp | ConvertTo-Json -Depth 5 | Out-File -Encoding UTF8 $loginRespFile
    Write-Host "Login response saved to $loginRespFile"
} catch {
    Write-Host "LOGIN_ERROR:"
    if ($_.Exception.Response -ne $null) { try { $sr = $_.Exception.Response.GetResponseStream(); $r = New-Object System.IO.StreamReader($sr); Write-Host $r.ReadToEnd(); } catch { Write-Host $_.Exception.Message } } else { Write-Host $_.Exception.Message }
    exit 1
}
try {
    $tok = $loginResp.accessToken
    if (-not $tok) { Write-Host "No accessToken in login response"; exit 1 }
    # Send the payload unchanged (allow server to accept array or string)
    $b = Get-Content -Raw $payloadFile
    Write-Host "Calling generate-from-payload..."
    $resp = Invoke-RestMethod -Uri 'http://localhost:8080/api/api/entity-management/generate-from-payload' -Method Post -Headers @{ Authorization = "Bearer $tok"; 'Content-Type'='application/json' } -Body $b -TimeoutSec 120
    $resp | ConvertTo-Json -Depth 5 | Write-Host
} catch {
    Write-Host "GENERATE_ERROR:"
    if ($_.Exception.Response -ne $null) { try { $sr = $_.Exception.Response.GetResponseStream(); $r = New-Object System.IO.StreamReader($sr); Write-Host $r.ReadToEnd(); } catch { Write-Host $_.Exception.Message } } else { Write-Host $_.Exception.Message }
    exit 1
}