Start-Sleep -Seconds 6
try {
  $h = Invoke-RestMethod -Uri 'http://localhost:8080/api/actuator/health' -UseBasicParsing -TimeoutSec 10
  Write-Output 'HEALTH_OK'
  $h | ConvertTo-Json -Depth 5
} catch {
  Write-Output 'HEALTH_ERROR'
  if ($_.Exception -ne $null) { Write-Output $_.Exception.Message } else { Write-Output 'no exception object' }
  $_ | ConvertTo-Json -Depth 5
}
