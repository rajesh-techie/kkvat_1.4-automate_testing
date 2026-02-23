# restart_services.ps1
# Stops and starts backend and frontend app processes using repo scripts.

# Stop backend java process matching the application jar/name
$p = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -match 'automation-platform' -and $_.Name -match 'java' }
if ($p) {
  $p | ForEach-Object {
    Write-Output "Stopping backend PID $($_.ProcessId)"
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
  }
} else {
  Write-Output "No backend java process found"
}

# Start backend using available repo start script
$backendPath = Join-Path $PSScriptRoot 'backend'
if (Test-Path $backendPath) {
  Set-Location $backendPath
  if (Test-Path .\"start-backend.bat\") {
    Write-Output "Starting backend via start-backend.bat"
    Start-Process -FilePath (Join-Path $backendPath 'start-backend.bat') -WorkingDirectory $backendPath
  } elseif (Test-Path .\"start-backend.ps1\") {
    Write-Output "Starting backend via start-backend.ps1"
    Start-Process -FilePath 'powershell' -ArgumentList "-NoProfile","-ExecutionPolicy","Bypass","-File","$backendPath\\start-backend.ps1"
  } else {
    Write-Output "No start-backend script found in $backendPath"
  }
} else {
  Write-Output "Backend folder not found: $backendPath"
}

# Stop frontend node processes associated with project
$p2 = Get-CimInstance Win32_Process | Where-Object { ($_.CommandLine -match 'kkvat-frontend' -or $_.CommandLine -match 'ng serve' -or $_.CommandLine -match 'npm run start') -and $_.Name -match 'node' }
if ($p2) {
  $p2 | ForEach-Object {
    Write-Output "Stopping frontend PID $($_.ProcessId)"
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
  }
} else {
  Write-Output "No frontend node process found"
}

# Start frontend using npm start if available
$frontendPath = Join-Path $PSScriptRoot 'frontend\kkvat-frontend'
if (Test-Path $frontendPath) {
  if (Test-Path (Join-Path $frontendPath 'package.json')) {
    Write-Output "Starting frontend with 'npm start' in $frontendPath"
    Start-Process -FilePath 'npm' -ArgumentList 'start' -WorkingDirectory $frontendPath
  } else {
    Write-Output "No package.json in frontend folder: $frontendPath"
  }
} else {
  Write-Output "Frontend folder not found: $frontendPath"
}
