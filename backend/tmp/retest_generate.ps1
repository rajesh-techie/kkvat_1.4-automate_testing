
$up=$false
for($i=0;$i -lt 15;$i++){
  $t=Test-NetConnection -ComputerName localhost -Port 8080
  if($t.TcpTestSucceeded){ $up=$true; break }
  Start-Sleep -Seconds 2
}
if(-not $up){ Write-Output 'PORT_DOWN'; exit 2 }
$loginPath = 'D:\\python_programs_rajesh\\kkvat_1.4-automate_testing\\backend\\tmp\\login_response.json'
$payloadPath = 'D:\\python_programs_rajesh\\kkvat_1.4-automate_testing\\backend\\tmp\\TBL_USERS_payload.json'
$tok=(Get-Content -Raw $loginPath | ConvertFrom-Json).accessToken
$b=Get-Content -Raw $payloadPath
Invoke-RestMethod -Uri 'http://localhost:8080/api/entity-management/generate-from-payload' -Method Post -Headers @{ 'Authorization'=("Bearer " + $tok); 'Content-Type'='application/json' } -Body $b -TimeoutSec 120 | ConvertTo-Json -Depth 5
