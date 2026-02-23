$loginPath='D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\login.json'
$respPath='D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\login_response.json'
$login = Get-Content -Raw $loginPath
$resp = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body $login -TimeoutSec 60
$resp | ConvertTo-Json -Depth 5 | Out-File -Encoding utf8 $respPath
Write-Output 'LOGIN_OK'
