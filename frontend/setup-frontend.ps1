# KKVat Automation Platform - Frontend Setup Script
# Run this script from the project root directory

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "KKVat Frontend Setup" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# Check if Node.js is installed
Write-Host "Checking Node.js installation..." -ForegroundColor Yellow
$nodeVersion = node --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Node.js is not installed!" -ForegroundColor Red
    Write-Host "Please install Node.js 18+ from https://nodejs.org/" -ForegroundColor Red
    exit 1
}
Write-Host "Node.js version: $nodeVersion" -ForegroundColor Green

# Check if Angular CLI is installed
Write-Host "Checking Angular CLI installation..." -ForegroundColor Yellow
$ngVersion = ng version --minimal 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Angular CLI not found. Installing..." -ForegroundColor Yellow
    npm install -g @angular/cli@17
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to install Angular CLI!" -ForegroundColor Red
        exit 1
    }
    Write-Host "Angular CLI installed successfully!" -ForegroundColor Green
} else {
    Write-Host "Angular CLI is already installed" -ForegroundColor Green
}

# Navigate to frontend directory
Set-Location -Path "frontend"

# Check if Angular project already exists
if (Test-Path "kkvat-frontend") {
    Write-Host ""
    Write-Host "WARNING: Frontend project already exists!" -ForegroundColor Yellow
    $response = Read-Host "Do you want to recreate it? (yes/no)"
    if ($response -eq "yes") {
        Write-Host "Removing existing project..." -ForegroundColor Yellow
        Remove-Item -Recurse -Force "kkvat-frontend"
    } else {
        Write-Host "Setup cancelled." -ForegroundColor Yellow
        exit 0
    }
}

# Create Angular application
Write-Host ""
Write-Host "Creating Angular application..." -ForegroundColor Yellow
Write-Host "This may take a few minutes..." -ForegroundColor Gray
ng new kkvat-frontend --routing --style=scss --skip-git --package-manager=npm

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to create Angular application!" -ForegroundColor Red
    exit 1
}

# Navigate into project
Set-Location -Path "kkvat-frontend"

# Install dependencies
Write-Host ""
Write-Host "Installing Angular Material..." -ForegroundColor Yellow
ng add @angular/material --theme=indigo-pink --animations=true --typography=true --skip-confirmation

Write-Host ""
Write-Host "Installing additional dependencies..." -ForegroundColor Yellow
npm install @ngrx/store @ngrx/effects @ngrx/store-devtools rxjs

# Create directory structure
Write-Host ""
Write-Host "Creating project structure..." -ForegroundColor Yellow

$directories = @(
    "src/app/core/auth",
    "src/app/core/models",
    "src/app/core/services",
    "src/app/core/guards",
    "src/app/core/interceptors",
    "src/app/shared/components",
    "src/app/shared/directives",
    "src/app/shared/pipes",
    "src/app/features/dashboard",
    "src/app/features/auth",
    "src/app/features/users",
    "src/app/features/groups",
    "src/app/features/test-cases",
    "src/app/features/reports",
    "src/app/features/audit",
    "src/app/layouts/header",
    "src/app/layouts/sidebar",
    "src/app/layouts/footer"
)

foreach ($dir in $directories) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}

Write-Host ""
Write-Host "===================================" -ForegroundColor Green
Write-Host "Frontend setup completed!" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. cd frontend\kkvat-frontend" -ForegroundColor White
Write-Host "2. ng serve" -ForegroundColor White
Write-Host "3. Open http://localhost:4200 in your browser" -ForegroundColor White
Write-Host ""
Write-Host "To proceed with Phase 1 implementation, let me know!" -ForegroundColor Yellow
