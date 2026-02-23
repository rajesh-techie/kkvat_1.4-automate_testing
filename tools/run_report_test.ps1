$ErrorActionPreference = 'Stop'
try {
  $login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body (@{ username = 'admin'; password = 'admin123' } | ConvertTo-Json) -ErrorAction Stop
  $token = $login.accessToken
  Write-Output "TOKENLEN:$($token.Length)"
  $body = @{ select_columns = @('id','username','email','firstName','lastName','role','isActive','createdAt'); view_id = 7; filter_condition = @{ email = 'admin' } } | ConvertTo-Json -Depth 10
  Write-Output "BODY:$body"
  try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:8080/api/report-executions/run/1' -Method Post -ContentType 'application/json' -Headers @{ Authorization = "Bearer $token" } -Body $body -ErrorAction Stop
    $resp | ConvertTo-Json -Depth 10
  } catch {
    Write-Error "RUN FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
      try { $_.Exception.Response.GetResponseStream() | ForEach-Object { (New-Object System.IO.StreamReader($_)).ReadToEnd() } | Write-Output } catch {}
    }
    exit 1
  }
} catch {
  Write-Error "LOGIN FAILED: $($_.Exception.Message)"
  if ($_.Exception.Response) { try { $_.Exception.Response.GetResponseStream() | ForEach-Object { (New-Object System.IO.StreamReader($_)).ReadToEnd() } | Write-Output } catch {} }
  exit 1
}
