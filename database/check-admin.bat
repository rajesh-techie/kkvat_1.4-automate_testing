@echo off
echo Checking admin user...
D:\python_programs_rajesh\server\mysql-enterprise-9.6.0_winx64_bundle\mysql-commercial-9.6.0-winx64\mysql-commercial-9.6.0-winx64\bin\mysql -u root -p -e "USE kkvat_automation; SELECT id, username, email, role, is_active FROM users WHERE username='admin';"
pause
