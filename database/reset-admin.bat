@echo off
echo Resetting admin password to Admin@123456...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -padmin kkvat_automation < update-password-final.sql
echo.
echo Password reset complete!
pause
