# start_frontend.ps1
$f = Join-Path (Get-Location) 'frontend\kkvat-frontend'
if (Test-Path $f) {
  Write-Output "Starting npm start in $f"
  Start-Process -FilePath 'npm' -ArgumentList 'start' -WorkingDirectory $f
} else {
  Write-Output "Frontend folder not found: $f"
}
Start-Sleep -Seconds 4
Get-CimInstance Win32_Process | Where-Object { $_.Name -match 'node' } | Select-Object ProcessId,Name,CommandLine -First 8 | Format-Table -AutoSize
