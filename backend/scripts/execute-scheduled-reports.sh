#!/bin/bash

# KKVat Automation Platform - Scheduled Report Executor
# This script runs as a cron job to execute scheduled reports
# 
# Usage: ./execute-scheduled-reports.sh
# 
# Crontab entry example:
# */5 * * * * /path/to/execute-scheduled-reports.sh
# (runs every 5 minutes)

set -euo pipefail

# ============================================================================
# Configuration
# ============================================================================

# API Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api}"
API_TOKEN="${API_TOKEN:-}"  # Set via environment variable or .env file

# Email Configuration
SEND_EMAIL="${SEND_EMAIL:-false}"
SMTP_FROM="${SMTP_FROM:-noreply@kkvat.local}"

# Logging Configuration
LOG_DIR="${LOG_DIR:-/var/log/kkvat}"
LOG_FILE="${LOG_DIR}/scheduled-reports.log"
LOG_LEVEL="${LOG_LEVEL:-INFO}"  # DEBUG, INFO, WARN, ERROR

# Timeout Configuration
CURL_TIMEOUT=30
MAX_RETRIES=3
RETRY_DELAY=5

# ============================================================================
# Logging Functions
# ============================================================================

log_debug() {
  if [[ "$LOG_LEVEL" == "DEBUG" ]]; then
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [DEBUG] $*" | tee -a "$LOG_FILE"
  fi
}

log_info() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] [INFO] $*" | tee -a "$LOG_FILE"
}

log_warn() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] [WARN] $*" | tee -a "$LOG_FILE"
}

log_error() {
  echo "[$(date +'%Y-%m-%d %H:%M:%S')] [ERROR] $*" | tee -a "$LOG_FILE" >&2
}

# ============================================================================
# Utility Functions
# ============================================================================

# Ensure log directory exists
ensure_log_dir() {
  if [[ ! -d "$LOG_DIR" ]]; then
    mkdir -p "$LOG_DIR"
    chmod 755 "$LOG_DIR"
  fi
}

# Rotate log file if too large (> 10MB)
rotate_log_file() {
  if [[ -f "$LOG_FILE" ]] && [[ $(stat -f%z "$LOG_FILE" 2>/dev/null || stat -c%s "$LOG_FILE" 2>/dev/null) -gt 10485760 ]]; then
    mv "$LOG_FILE" "$LOG_FILE.$(date +%Y%m%d_%H%M%S)"
    gzip "$LOG_FILE".* && rm "$LOG_FILE".*.gz || true
    log_info "Log file rotated"
  fi
}

# Load configuration from .env file if it exists
load_env_file() {
  local env_file="$(dirname "$0")/.env"
  if [[ -f "$env_file" ]]; then
    log_debug "Loading configuration from $env_file"
    set -a
    source "$env_file"
    set +a
  fi
}

# Validate required configuration
validate_config() {
  if [[ -z "$API_TOKEN" ]]; then
    log_error "API_TOKEN not set. Set it via environment variable or .env file"
    return 1
  fi
  return 0
}

# Retry function with exponential backoff
retry_curl() {
  local method=$1
  local url=$2
  local data=${3:-}
  local attempt=1
  
  while [[ $attempt -le $MAX_RETRIES ]]; do
    local response
    if [[ -n "$data" ]]; then
      response=$(curl -s -w "\n%{http_code}" \
        -X "$method" \
        -H "Authorization: Bearer $API_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$data" \
        --max-time "$CURL_TIMEOUT" \
        "$url" 2>/dev/null || echo "")
    else
      response=$(curl -s -w "\n%{http_code}" \
        -X "$method" \
        -H "Authorization: Bearer $API_TOKEN" \
        --max-time "$CURL_TIMEOUT" \
        "$url" 2>/dev/null || echo "")
    fi
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | head -n-1)
    
    if [[ "$http_code" =~ ^[23][0-9]{2}$ ]]; then
      echo "$body"
      return 0
    fi
    
    log_warn "HTTP $http_code on attempt $attempt/$MAX_RETRIES for $url"
    
    if [[ $attempt -lt $MAX_RETRIES ]]; then
      sleep "$RETRY_DELAY"
    fi
    
    ((attempt++))
  done
  
  log_error "Failed to execute request after $MAX_RETRIES attempts: $url"
  return 1
}

# Send email notification
send_email() {
  local recipient=$1
  local subject=$2
  local body=$3
  
  if [[ "$SEND_EMAIL" != "true" ]]; then
    return 0
  fi
  
  # Try multiple methods to send email
  if command -v mail &> /dev/null; then
    echo "$body" | mail -s "$subject" -r "$SMTP_FROM" "$recipient"
  elif command -v sendmail &> /dev/null; then
    {
      echo "From: $SMTP_FROM"
      echo "To: $recipient"
      echo "Subject: $subject"
      echo ""
      echo "$body"
    } | sendmail "$recipient"
  else
    log_warn "Email not available (mail/sendmail not found), skipping notification to $recipient"
  fi
}

# ============================================================================
# Main Logic
# ============================================================================

fetch_schedules() {
  log_debug "Fetching pending schedules from $API_BASE_URL/report-schedules"
  
  local response=$(retry_curl "GET" "$API_BASE_URL/report-schedules?page=0&size=100") || return 1
  echo "$response"
}

execute_schedule() {
  local schedule_id=$1
  local schedule_name=$2
  local email_recipients=$3
  
  log_info "Executing schedule $schedule_id: $schedule_name"
  
  # Get the schedule details to find the report ID
  local schedule_response=$(retry_curl "GET" "$API_BASE_URL/report-schedules/$schedule_id") || return 1
  local report_id=$(echo "$schedule_response" | grep -o '"reportId":[0-9]*' | cut -d':' -f2)
  
  if [[ -z "$report_id" ]]; then
    log_error "Could not extract reportId from schedule $schedule_id"
    return 1
  fi
  
  # Trigger report generation
  local execution_response=$(retry_curl "POST" "$API_BASE_URL/report-executions/generate/$report_id") || return 1
  local execution_id=$(echo "$execution_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  
  if [[ -z "$execution_id" ]]; then
    log_error "Could not extract executionId from response"
    return 1
  fi
  
  log_info "Report generation triggered: execution_id=$execution_id, report_id=$report_id"
  
  # Wait for completion and send email notification if configured
  wait_for_completion "$execution_id" "$schedule_name" "$email_recipients" &
  
  return 0
}

wait_for_completion() {
  local execution_id=$1
  local schedule_name=$2
  local email_recipients=$3
  
  local max_wait=300  # 5 minutes
  local elapsed=0
  local poll_interval=5
  
  log_debug "Waiting for execution $execution_id to complete (max ${max_wait}s)"
  
  while [[ $elapsed -lt $max_wait ]]; do
    sleep "$poll_interval"
    ((elapsed += poll_interval))
    
    local execution=$(retry_curl "GET" "$API_BASE_URL/report-executions/$execution_id") || continue
    local status=$(echo "$execution" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    
    log_debug "Execution $execution_id status: $status"
    
    if [[ "$status" == "COMPLETED" ]]; then
      local file_path=$(echo "$execution" | grep -o '"filePath":"[^"]*"' | cut -d'"' -f4)
      local row_count=$(echo "$execution" | grep -o '"rowCount":[0-9]*' | cut -d':' -f2)
      
      log_info "Report generation completed: execution_id=$execution_id, rows=$row_count, file=$file_path"
      
      # Send email notification if recipients configured
      if [[ -n "$email_recipients" ]] && [[ "$email_recipients" != "null" ]]; then
        local email_body="Report '$schedule_name' has been generated successfully.\\n\\nRows: $row_count\\nFile: $file_path"
        
        IFS=',' read -ra recipients <<< "$email_recipients"
        for recipient in "${recipients[@]}"; do
          recipient=$(echo "$recipient" | xargs)  # Trim whitespace
          send_email "$recipient" "Report Generated: $schedule_name" "$email_body"
          log_info "Email notification sent to $recipient"
        done
      fi
      
      return 0
    elif [[ "$status" == "FAILED" ]]; then
      local error_msg=$(echo "$execution" | grep -o '"errorMessage":"[^"]*"' | cut -d'"' -f4)
      log_error "Report generation failed: execution_id=$execution_id, error=$error_msg"
      
      # Send failure notification
      if [[ -n "$email_recipients" ]] && [[ "$email_recipients" != "null" ]]; then
        local email_body="Report '$schedule_name' generation FAILED.\\n\\nError: $error_msg"
        
        IFS=',' read -ra recipients <<< "$email_recipients"
        for recipient in "${recipients[@]}"; do
          recipient=$(echo "$recipient" | xargs)
          send_email "$recipient" "Report Generation Failed: $schedule_name" "$email_body"
        done
      fi
      
      return 1
    fi
  done
  
  log_warn "Timeout waiting for execution $execution_id to complete"
  return 1
}

main() {
  ensure_log_dir
  rotate_log_file
  load_env_file
  
  log_info "=== Scheduled Report Executor started ==="
  
  if ! validate_config; then
    log_error "Configuration validation failed"
    exit 1
  fi
  
  # Fetch all schedules
  local schedules_response=$(fetch_schedules) || {
    log_error "Failed to fetch schedules"
    exit 1
  }
  
  # Parse schedule entries
  # Note: This is a simple JSON parsing using grep. For production, consider using jq
  local schedule_ids=$(echo "$schedules_response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
  local count=0
  
  if [[ -z "$schedule_ids" ]]; then
    log_info "No pending schedules found"
    exit 0
  fi
  
  while IFS= read -r schedule_id; do
    if [[ -z "$schedule_id" ]]; then
      continue
    fi
    
    # Extract schedule details from the response
    local schedule_name=$(echo "$schedules_response" | grep -A 5 "\"id\":$schedule_id" | grep -o '"scheduleName":"[^"]*"' | cut -d'"' -f4 | head -1)
    local email_recipients=$(echo "$schedules_response" | grep -A 5 "\"id\":$schedule_id" | grep -o '"emailRecipients":"[^"]*"' | cut -d'"' -f4 | head -1)
    
    if execute_schedule "$schedule_id" "$schedule_name" "$email_recipients"; then
      ((count++))
    fi
  done <<< "$schedule_ids"
  
  log_info "Executor completed: $count schedules executed"
  log_info "=== Scheduled Report Executor finished ==="
}

# ============================================================================
# Entry Point
# ============================================================================

main "$@"
