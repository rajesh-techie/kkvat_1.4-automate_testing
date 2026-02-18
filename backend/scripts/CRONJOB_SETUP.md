# Cronjob Setup Guide for Scheduled Report Execution

## Overview
This guide explains how to configure the bash cronjob script to execute scheduled reports automatically.

---

## Prerequisites

1. Linux/Unix-based system with bash
2. `curl` command-line tool installed
3. Backend API running at the configured URL
4. Valid JWT token for API authentication

---

## Installation Steps

### 1. Copy Configuration File
```bash
cp /path/to/scripts/.env.example /path/to/scripts/.env
chmod 600 /path/to/scripts/.env  # Restrict permissions
```

### 2. Edit Configuration
```bash
vi /path/to/scripts/.env
```

Update the following values:
- `API_BASE_URL`: Your backend API URL (e.g., http://localhost:8080/api)
- `API_TOKEN`: Your JWT authentication token
- `LOG_DIR`: Directory for logs (default: /var/log/kkvat)
- `SEND_EMAIL`: Set to true if you want email notifications

### 3. Make Script Executable
```bash
chmod +x /path/to/scripts/execute-scheduled-reports.sh
```

### 4. Create Log Directory
```bash
sudo mkdir -p /var/log/kkvat
sudo chown $(whoami) /var/log/kkvat
chmod 755 /var/log/kkvat
```

---

## Cronjob Configuration

### Option 1: Every 5 Minutes (Recommended)
```bash
*/5 * * * * /path/to/scripts/execute-scheduled-reports.sh
```

### Option 2: Every Hour
```bash
0 * * * * /path/to/scripts/execute-scheduled-reports.sh
```

### Option 3: Every Minute (Higher Frequency)
```bash
* * * * * /path/to/scripts/execute-scheduled-reports.sh
```

### Option 4: Business Hours Only (Every 5 minutes, Mon-Fri, 8-18)
```bash
*/5 8-18 * * 1-5 /path/to/scripts/execute-scheduled-reports.sh
```

### Full Crontab Entry with Email Notification
```bash
# Send errors to system admin
MAILTO=admin@example.com

*/5 * * * * /path/to/scripts/execute-scheduled-reports.sh 2>&1
```

---

## Adding to Crontab

### Step 1: Open Crontab Editor
```bash
crontab -e
```

### Step 2: Add the Cronjob Entry
Use one of the examples above. Here's a complete example:

```bash
# KKVat Scheduled Report Executor
# Runs every 5 minutes to execute scheduled reports
*/5 * * * * /path/to/backend/scripts/execute-scheduled-reports.sh
```

### Step 3: Verify Installation
```bash
crontab -l  # List all cron jobs
```

---

## Obtaining JWT Token

To get a valid JWT token for API authentication:

### 1. Login via API
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123456"
  }'
```

### 2. Extract Token from Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

### 3. Store in .env File
```bash
API_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Refresh Token Periodically
Note: JWT tokens expire. Set up a mechanism to refresh the token periodically:

```bash
#!/bin/bash
# Token refresh script (run daily)
API_BASE_URL="http://localhost:8080/api"
RESPONSE=$(curl -s -X POST "$API_BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "YourPassword"
  }')

NEW_TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
sed -i "s/API_TOKEN=.*/API_TOKEN=\"$NEW_TOKEN\"/" /path/to/scripts/.env
```

Add this to crontab to refresh daily at 4 AM:
```bash
0 4 * * * /path/to/scripts/refresh-token.sh
```

---

## Verification and Testing

### 1. Test Script Manually
```bash
# Set environment and run
export $(cat /path/to/scripts/.env | xargs)
/path/to/scripts/execute-scheduled-reports.sh
```

### 2. Monitor Logs
```bash
# Follow logs in real-time
tail -f /var/log/kkvat/scheduled-reports.log

# Search for errors
grep ERROR /var/log/kkvat/scheduled-reports.log

# Check last 50 lines
tail -50 /var/log/kkvat/scheduled-reports.log
```

### 3. Verify Cron Execution
```bash
# Check system cron logs (Linux)
grep "execute-scheduled-reports" /var/log/syslog

# Or for macOS
log stream --predicate 'process == "cron"'
```

### 4. Create Test Schedule
1. Create a report via API or UI
2. Create a schedule with:
   - Frequency: DAILY
   - Time: 5 minutes from now
3. Wait and check logs for execution

---

## Troubleshooting

### Issue: "API_TOKEN not set"
**Solution**: Ensure .env file is properly loaded and contains API_TOKEN

```bash
cat /path/to/scripts/.env
# Check for API_TOKEN line
grep API_TOKEN /path/to/scripts/.env
```

### Issue: "Failed to fetch schedules"
**Solution**: Verify API connectivity
```bash
curl -s -X GET "http://localhost:8080/api/report-schedules" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Issue: Cron job not executing
**Solution**: Check cron service and permissions
```bash
# Verify cron is running
sudo service cron status  # Linux
sudo launchctl list | grep cron  # macOS

# Check cron permissions
crontab -u $USER -l

# Verify script permissions
ls -l /path/to/scripts/execute-scheduled-reports.sh
# Should have execute bit: -rwxr-xr-x
```

### Issue: Email notifications not working
**Solution**: Verify mail/sendmail is available
```bash
which mail
which sendmail
# If not found, install:
# sudo apt-get install mailutils  (Debian/Ubuntu)
# brew install mailutils  (macOS)
```

### Issue: "Connection refused"
**Solution**: Ensure backend API is running
```bash
curl http://localhost:8080/api/reports
# Should return data, not connection error
```

---

## Performance Tuning

### 1. Adjust Execution Frequency
- **Every minute**: More responsive but higher load
- **Every 5 minutes**: Balanced (recommended)
- **Every 15 minutes**: Lower load but slower execution

### 2. Parallel Execution Limits
Edit script to limit concurrent executions:
```bash
# Maximum 3 concurrent report generations
pgrep -f "execute-scheduled-reports" | wc -l
if [[ $(pgrep -f "execute-scheduled-reports" | wc -l) -gt 3 ]]; then
  exit 0
fi
```

### 3. Log Rotation
The script auto-rotates logs when > 10MB. To customize:
```bash
# Edit in script:
if [[ $(stat -c%s "$LOG_FILE") -gt 52428800 ]]; then  # 50MB
  # rotate
fi
```

---

## Security Considerations

1. **Secure .env File**
   ```bash
   chmod 600 /path/to/scripts/.env
   ```

2. **Use Environment Variables Instead of .env**
   ```bash
   export API_TOKEN="your_token"
   export API_BASE_URL="http://localhost:8080/api"
   ```

3. **Run as Dedicated User**
   ```bash
   # Create user
   sudo useradd -r -s /bin/false kkvat-scheduler
   
   # Grant permissions
   sudo chown kkvat-scheduler /path/to/scripts
   
   # Crontab
   sudo crontab -u kkvat-scheduler -e
   ```

4. **Rotate API Tokens Regularly**
   - Set token expiration in backend configuration
   - Refresh tokens via scheduled script (see earlier)

---

## Monitoring and Alerting

### 1. Email Alerts on Script Failure
```bash
*/5 * * * * /path/to/scripts/execute-scheduled-reports.sh 2>&1 | \
  grep -q ERROR && mail -s "Report Scheduler Error" admin@example.com
```

### 2. Syslog Integration
```bash
# Log to syslog
logger -t kkvat-scheduler "Report generation completed"
```

### 3. Slack/Teams Integration
```bash
# Send notification to Slack
curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK \
  -d '{"text":"Report scheduler executed successfully"}'
```

---

## Example Setup (Complete)

```bash
#!/bin/bash
# Complete setup script

SCRIPT_DIR="/opt/kkvat/scripts"
LOG_DIR="/var/log/kkvat"
USER="kkvat"

# 1. Create directories
sudo mkdir -p "$SCRIPT_DIR" "$LOG_DIR"

# 2. Copy files
sudo cp execute-scheduled-reports.sh "$SCRIPT_DIR/"
sudo cp .env.example "$SCRIPT_DIR/.env"

# 3. Set permissions
sudo chmod 755 "$SCRIPT_DIR/execute-scheduled-reports.sh"
sudo chmod 600 "$SCRIPT_DIR/.env"
sudo chown -R "$USER:$USER" "$SCRIPT_DIR" "$LOG_DIR"

# 4. Configure .env
sudo -u "$USER" vi "$SCRIPT_DIR/.env"

# 5. Install crontab
echo "*/5 * * * * $SCRIPT_DIR/execute-scheduled-reports.sh" | \
  sudo crontab -u "$USER" -

# 6. Verify
sudo crontab -u "$USER" -l
```

---

## Support

For issues or questions:
1. Check logs: `tail -f /var/log/kkvat/scheduled-reports.log`
2. Enable debug logging: Set `LOG_LEVEL=DEBUG` in .env
3. Test API connectivity manually
4. Review crontab configuration

---

**Last Updated**: February 18, 2026
**Script Version**: Phase 4.0
