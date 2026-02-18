#!/bin/bash
# KKVat Frontend Quick Setup Script

echo "=== KKVat Automation Platform - Frontend Setup ==="
echo ""
echo "Prerequisites:"
echo "  - Node.js 18+ and npm"
echo "  - Angular CLI 17"
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+ from https://nodejs.org/"
    exit 1
fi

echo "✓ Node.js $(node --version) found"

# Check if Angular CLI is installed
if ! command -v ng &> /dev/null; then
    echo ""
    echo "Installing Angular CLI 17 globally..."
    npm install -g @angular/cli@17
fi

echo "✓ Angular CLI found"

# Navigate to frontend directory
cd d:/python_programs_rajesh/kkvat_1.4-automate_testing/frontend

echo ""
echo "Creating Angular 17 application..."
echo ""

# Create Angular app if not exists
if [ ! -d "kkvat-frontend" ]; then
    ng new kkvat-frontend \
        --routing \
        --style=scss \
        --skip-git \
        --package-manager=npm \
        --skip-install
    
    cd kkvat-frontend
    
    echo ""
    echo "Installing dependencies..."
    echo "  - Angular Material & CDK"
    echo "  - NGRX Store"
    echo "  - TypeScript dependencies"
    echo ""
    
    npm install \
        @angular/material@17 \
        @angular/cdk@17 \
        @angular/animations@17 \
        @ngrx/store@17 \
        @ngrx/effects@17 \
        @ngrx/store-devtools@17 \
        rxjs \
        --legacy-peer-deps
    
    npm install --save-dev @types/node
else
    cd kkvat-frontend
fi

echo ""
echo "=================================="
echo "✓ Setup Complete!"
echo "=================================="
echo ""
echo "To start the frontend development server:"
echo ""
echo "  cd d:/python_programs_rajesh/kkvat_1.4-automate_testing/frontend/kkvat-frontend"
echo "  npm start"
echo ""
echo "Frontend URL: http://localhost:4200"
echo "Backend API:  http://localhost:8080/api"
echo ""
echo "Login Credentials:"
echo "  Username: admin"
echo "  Password: admin123"
echo ""
