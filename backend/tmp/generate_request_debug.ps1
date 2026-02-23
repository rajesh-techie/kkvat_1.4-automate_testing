$loginPath='D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\login_response.json'
$payloadPath='D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\TBL_USERS_payload.json'
$respPath='D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\generate_response.html'
$tok=(Get-Content -Raw $loginPath | ConvertFrom-Json).accessToken
$b=Get-Content -Raw $payloadPath
$response = Invoke-WebRequest -Uri 'http://localhost:8080/api/entity-management/generate-from-payload' -Method Post -Headers @{ 'Authorization'=("Bearer " + $tok); 'Content-Type'='application/json' } -Body $b -TimeoutSec 120 -ErrorAction SilentlyContinue
$info = @{ Status = $response.StatusCode; StatusDescription = $response.StatusDescription }
$info | ConvertTo-Json | Out-File -Encoding utf8 D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\generate_response_info.json
$response.Content | Out-File -Encoding utf8 $respPath
Write-Output 'DONE'