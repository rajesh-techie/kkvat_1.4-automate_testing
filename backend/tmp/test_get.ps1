$tok=(Get-Content -Raw 'D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\login_response.json' | ConvertFrom-Json).accessToken
$response = Invoke-RestMethod -Uri 'http://localhost:8080/api/entity-management' -Method Get -Headers @{ 'Authorization' = ("Bearer " + $tok) } -ErrorAction SilentlyContinue
$response | ConvertTo-Json -Depth 5 | Out-File -Encoding utf8 D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\list_response.json
Write-Output 'DONE'