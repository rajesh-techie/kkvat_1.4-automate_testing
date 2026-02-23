$ErrorActionPreference = 'Stop'
$tokenFile = 'tmp/login_response.json'
$payloadFile = 'tmp/TBL_USERS_payload.json'
if (-Not (Test-Path $tokenFile)) { Write-Host "Token file not found: $tokenFile"; exit 1 }
if (-Not (Test-Path $payloadFile)) { Write-Host "Payload file not found: $payloadFile"; exit 1 }
$tok = (Get-Content -Raw $tokenFile | ConvertFrom-Json).accessToken
$b = Get-Content -Raw $payloadFile
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:8080/api/api/entity-management/generate-from-payload' -Method Post -Headers @{ Authorization = "Bearer $tok"; 'Content-Type' = 'application/json' } -Body $b -TimeoutSec 120
    $resp | ConvertTo-Json -Depth 5 | Write-Host
} catch {
    Write-Host "HTTP_ERROR:"
    if ($_.Exception.Response -ne $null) {
        try { $sr = $_.Exception.Response.GetResponseStream(); $r = New-Object System.IO.StreamReader($sr); Write-Host $r.ReadToEnd(); } catch { Write-Host $_.Exception.Message }
    } else { Write-Host $_.Exception.Message }
    exit 1
}