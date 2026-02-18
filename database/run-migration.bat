@echo off
echo Migrating recording_sessions table...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -p < migrate-recording-sessions.sql
echo.
echo Migration complete!
pause
