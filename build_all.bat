@echo off
setlocal

REM build_all.bat - build backend (Maven) and frontend (npm/Angular)
REM Steps are numbered and will stop on first failure.

echo ==================================================
echo kkvat - full build script
echo ==================================================
echo 1) Build backend with Maven
echo 2) Install frontend dependencies (npm)
echo 3) Build frontend
echo.

:: Step 1: Build backend
echo [1/3] Building backend (Maven)...
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
  echo ERROR: 'mvn' not found in PATH. Install Maven and try again.
  goto :error
)
if not exist backend (
  echo ERROR: backend directory not found.
  goto :error
)
pushd backend
echo Running: mvn -DskipTests clean package
mvn -DskipTests clean package
if %ERRORLEVEL% neq 0 (
  echo ERROR: Maven build failed.
  popd
  goto :error
)
popd
echo Backend build succeeded.
echo.

:: Step 2: Install frontend deps
echo [2/3] Installing frontend dependencies (npm)...
where npm >nul 2>&1
if %ERRORLEVEL% neq 0 (
  echo ERROR: 'npm' not found in PATH. Install Node.js (includes npm) and try again.
  goto :error
)
if not exist frontend\kkvat-frontend (
  echo ERROR: frontend/kkvat-frontend directory not found.
  goto :error
)
pushd frontend\kkvat-frontend
echo Running: npm ci --no-audit --no-fund
npm ci --no-audit --no-fund
if %ERRORLEVEL% neq 0 (
  echo npm install failed, trying 'npm install' as fallback...
  npm install --no-audit --no-fund
  if %ERRORLEVEL% neq 0 (
    echo ERROR: npm install failed.
    popd
    goto :error
  )
)
echo Dependencies installed.
echo.

:: Step 3: Build frontend
echo [3/3] Building frontend (ng build / npm run build)...
echo Running: npm run build --if-present
npm run build --if-present
if %ERRORLEVEL% neq 0 (
  echo ERROR: Frontend build failed.
  popd
  goto :error
)
popd
echo Frontend build succeeded.
echo.

echo All steps completed successfully.
endlocal
exit /b 0

:error
echo One or more steps failed. See messages above.
endlocal
exit /b 1
