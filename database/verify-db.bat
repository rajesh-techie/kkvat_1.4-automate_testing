@echo off
echo Verifying database creation...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -p -e "USE kkvat_automation; SHOW TABLES;"
pause
