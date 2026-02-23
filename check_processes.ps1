# check_processes.ps1
Write-Output 'Node processes:'
Get-CimInstance Win32_Process | Where-Object { $_.Name -match 'node' } | Select-Object ProcessId,Name,CommandLine -First 10 | Format-Table -AutoSize
Write-Output 'Java processes:'
Get-CimInstance Win32_Process | Where-Object { $_.Name -match 'java' } | Select-Object ProcessId,Name,CommandLine -First 10 | Format-Table -AutoSize
