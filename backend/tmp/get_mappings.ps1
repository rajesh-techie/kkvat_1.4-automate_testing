$loginPath='D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend\tmp\login_response.json'
$tok=(Get-Content -Raw $loginPath | ConvertFrom-Json).accessToken
try {
  $m = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/mappings' -Method Get -Headers @{ Authorization = ('Bearer ' + $tok) } -ErrorAction Stop
  $m | ConvertTo-Json -Depth 6
} catch { Write-Output $_.Exception.Response.StatusCode.Value__ ; $_ | Select-Object * -ExcludeProperty InvocationInfo | ConvertTo-Json -Depth 6 }
