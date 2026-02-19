@echo off
echo KKVat Database Migration Script
echo.
echo This script will initialize/migrate the database schema
echo.
echo Checking if schema.sql exists...
if not exist "schema.sql" (
    echo ERROR: schema.sql not found!
    pause
    exit /b 1
)

echo Running full database schema migration...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -p < schema.sql

echo.
echo Schema migration complete!
echo.
echo Running additional migrations...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -p < migrate-recording-sessions.sql

echo.
echo Creating report views (run create_report_views.sql) using root/admin...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -padmin < create_report_views.sql

echo.
echo All migrations complete!
pause
