$loginPath='D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\login_response.json'
$tok=(Get-Content -Raw $loginPath | ConvertFrom-Json).accessToken
$m = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/mappings' -Method Get -Headers @{ Authorization = ('Bearer ' + $tok) }
$out = $m | Out-String -Width 4096
$out | Select-String -Pattern 'entity-management' -Context 2,2 | ForEach-Object { $_.Line; $_.Context.PreContext; $_.Context.PostContext }
