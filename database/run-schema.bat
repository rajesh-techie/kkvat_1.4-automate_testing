@echo off
echo Creating database and tables for KKVat Automation Platform...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -p < schema.sql
echo.
echo Database setup complete!
echo Please verify by checking for kkvat_automation database in MySQL.
pause
