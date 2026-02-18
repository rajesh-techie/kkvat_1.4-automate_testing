# Start KKVat Automation Platform Backend
Write-Host "Starting KKVat Automation Platform Backend..." -ForegroundColor Green
Write-Host ""
Write-Host "Please make sure MySQL is running." -ForegroundColor Yellow
Write-Host ""

$dbPassword = Read-Host "Enter MySQL root password" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
$plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

Write-Host ""
Write-Host "Starting backend..." -ForegroundColor Green

$env:DB_PASSWORD = $plainPassword
Set-Location D:\python_programs_rajesh\kkvat_1.4-automate_testing\backend
java -jar target\automation-platform-1.4.0.jar
