# start_services.ps1
# Starts backend (spring boot) and frontend (Angular) located at frontend\llvat_frontend

$repo = (Get-Location).Path
$backend = Join-Path $repo 'backend'
Write-Output "--- Backend: checking $backend ---"
if (Test-Path (Join-Path $backend 'start-backend.bat')) {
  Write-Output 'Found start-backend.bat; launching'
  Start-Process -FilePath (Join-Path $backend 'start-backend.bat') -WorkingDirectory $backend
} elseif (Test-Path (Join-Path $backend 'start-backend.ps1')) {
  Write-Output 'Found start-backend.ps1; launching'
  Start-Process -FilePath 'powershell' -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-File',(Join-Path $backend 'start-backend.ps1') -WorkingDirectory $backend
} else {
  $target = Join-Path $backend 'target'
  $jars = @()
  if (Test-Path $target) {
    $jars = Get-ChildItem -Path $target -Filter '*.jar' -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike '*.original' }
  }
  if ((-not $jars) -and (Test-Path $target)) {
    $jars = Get-ChildItem -Path $target -Filter '*.jar' -Recurse -ErrorAction SilentlyContinue
  }
  if ($jars -and $jars.Count -gt 0) {
    $jar = $jars | Select-Object -First 1
    Write-Output ("Launching java -jar " + $jar.FullName)
    Start-Process -FilePath 'java' -ArgumentList '-jar', $jar.FullName -WorkingDirectory (Split-Path $jar.FullName)
  } elseif (Test-Path (Join-Path $backend 'pom.xml')) {
    Write-Output 'No jar found; attempting mvn spring-boot:run'
    Start-Process -FilePath 'mvn' -ArgumentList '-f', (Join-Path $backend 'pom.xml'), 'spring-boot:run' -WorkingDirectory $backend
  } else {
    Write-Output 'No start script, jar, or pom.xml found for backend'
  }
}

# Frontend
$frontend = Join-Path $repo 'frontend\llvat_frontend'
Write-Output "--- Frontend: checking $frontend ---"
if (Test-Path $frontend) {
  if (Test-Path (Join-Path $frontend 'package.json')) {
    Write-Output 'Found package.json; launching npm start'
    Start-Process -FilePath 'npm' -ArgumentList 'start' -WorkingDirectory $frontend
  } else {
    Write-Output 'No package.json in frontend path'
  }
} else {
  Write-Output ('Frontend folder not found: ' + $frontend)
}

# Short verification
Start-Sleep -Seconds 4
Write-Output '--- Verification (process list snippets) ---'
$pJava = Get-CimInstance Win32_Process | Where-Object { ($_.Name -match 'java') -and ($_.CommandLine -match 'automation-platform' -or $_.CommandLine -match 'spring' -or $_.CommandLine -match 'backend') } | Select-Object ProcessId,Name,CommandLine -First 8
if ($pJava) { $pJava | Format-Table -AutoSize } else { Write-Output 'No matching java processes found (by automation-platform/spring/backend filter)'; Get-CimInstance Win32_Process | Where-Object { $_.Name -match 'java' } | Select-Object -First 5 | Format-Table -AutoSize }
$pNode = Get-CimInstance Win32_Process | Where-Object { ($_.Name -match 'node') -and ($_.CommandLine -match 'llvat_frontend' -or $_.CommandLine -match 'ng' -or $_.CommandLine -match 'npm') } | Select-Object ProcessId,Name,CommandLine -First 8
if ($pNode) { $pNode | Format-Table -AutoSize } else { Write-Output 'No matching node processes found (by llvat_frontend/ng/npm filter)'; Get-CimInstance Win32_Process | Where-Object { $_.Name -match 'node' } | Select-Object -First 5 | Format-Table -AutoSize }
