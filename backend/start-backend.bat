@echo off
echo Starting KKVat Automation Platform Backend...
echo.
echo Please make sure MySQL is running and you have the correct root password.
echo.
set /p DB_PASS="Enter MySQL root password: "
echo.
echo Starting backend with provided password...
set DB_PASSWORD=%DB_PASS%
java -jar target\automation-platform-1.4.0.jar
