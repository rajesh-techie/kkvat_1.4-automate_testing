# KKVat Automation Platform - Phase 4 Implementation Summary
## Report Generation & Scheduling

**Status**: ✅ Backend Complete | 🔄 Frontend In Progress | ⏳ Scripts Pending

---

## Overview

Phase 4 implements a complete reporting system for the KKVat Automation Platform with:
- **Report Templates Builder** - Create, modify, delete report configurations
- **Report Scheduling** - Schedule reports for daily/weekly/monthly/annual execution
- **Report Execution Engine** - Generate CSV reports with filtering, sorting
- **Report History** - Track all generated reports with download capability
- **Flexible API-driven Architecture** - Complete REST API for all operations

---

## 1. Database Schema (COMPLETED)

### New Tables Created

#### `report_views`
Defines available data sources for report building
```sql
- id (PK)
- name, display_name, description
- table_name (actual DB table)
- is_active
- created_at, updated_at
```
**Pre-populated Views:**
- "test_executions_view" → test_executions table
- "user_activity_view" → audit_logs table
- "test_cases_view" → test_cases table

#### `report_view_fields`
Field/column definitions for each report view
```sql
- id (PK)
- view_id (FK)
- field_name, display_name
- field_type (STRING, NUMBER, DATE, BOOLEAN, etc.)
- is_filterable, is_sortable
```

#### `reports` (Modified)
Report templates with configuration
```sql
- id, name, description
- view_id (FK) - data source
- selected_columns (JSON) - ["col1", "col2", "col3"]
- filter_conditions (JSON) - {"field": "value"}
- sort_config (JSON) - {"field": "ASC"}
- report_type (EXECUTION, USER_ACTIVITY, CUSTOM)
- is_public
- created_by/updated_by (FK to users)
```

#### `report_schedules`
Schedule configurations for recurring report generation
```sql
- id, report_id (FK)
- schedule_name
- frequency (DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY)
- day_of_week (0-6 for WEEKLY)
- day_of_month (1-31 for MONTHLY/QUARTERLY/ANNUALLY)
- time_of_day (HH:mm:ss)
- email_recipients (comma-separated)
- is_active, last_executed, next_execution
- created_by/updated_by (FK)
```

#### `report_executions`
Execution history and download management
```sql
- id, report_id (FK), schedule_id (FK)
- execution_type (MANUAL, SCHEDULED, API)
- status (PENDING, GENERATING, COMPLETED, FAILED)
- start_time, end_time, duration_ms
- file_path (CSV location), file_size, row_count
- error_message
- executed_by (FK to users)
```

---

## 2. Spring Boot APIs (19 Endpoints - COMPLETED)

### A. Report Views API
**Base Path:** `/api/report-views`

| Method | Endpoint | Purpose | Auth |
|--------|----------|---------|------|
| GET | `/api/report-views` | List all available report data sources | VIEWER+ |
| GET | `/api/report-views/{id}` | Get specific view with fields | VIEWER+ |
| GET | `/api/report-views/name/{name}` | Get view by name | VIEWER+ |
| GET | `/api/report-views/{viewId}/fields` | Get all fields for a view | VIEWER+ |
| GET | `/api/report-views/{viewId}/fields/filterable` | Get filterable fields only | VIEWER+ |
| GET | `/api/report-views/{viewId}/fields/sortable` | Get sortable fields only | VIEWER+ |

**Example Response:**
```json
{
  "id": 1,
  "name": "test_executions_view",
  "displayName": "Test Executions",
  "fields": [
    {
      "id": 1,
      "fieldName": "id",
      "displayName": "Execution ID",
      "fieldType": "NUMBER",
      "isFilterable": true,
      "isSortable": true
    }
  ]
}
```

### B. Reports API (Report Templates - CRUD)
**Base Path:** `/api/reports`

| Method | Endpoint | Purpose | Auth |
|--------|----------|---------|------|
| GET | `/api/reports` | List all accessible reports (paginated) | VIEWER+ |
| GET | `/api/reports/{id}` | Get report template details | VIEWER+ |
| POST | `/api/reports` | Create new report template | TESTER+ |
| PUT | `/api/reports/{id}` | Modify report template | TESTER+ |
| DELETE | `/api/reports/{id}` | Delete report template | ADMIN/TEST_MANAGER |
| GET | `/api/reports/search` | Search reports by name/description | VIEWER+ |
| GET | `/api/reports/view/{viewId}` | Get reports for specific view | VIEWER+ |

**Create Report Request Body:**
```json
{
  "name": "Daily Test Results",
  "description": "All test executions from today",
  "viewId": 1,
  "selectedColumns": ["id", "status", "duration_ms", "browser"],
  "filterConditions": {
    "status": "PASSED"
  },
  "sortConfig": {
    "id": "DESC"
  },
  "reportType": "EXECUTION",
  "isPublic": false
}
```

### C. Report Schedules API
**Base Path:** `/api/report-schedules`

| Method | Endpoint | Purpose | Auth |
|--------|----------|---------|------|
| GET | `/api/report-schedules` | List all schedules (paginated) | TESTER+ |
| GET | `/api/report-schedules/{id}` | Get schedule details | TESTER+ |
| POST | `/api/report-schedules` | Create new schedule | TESTER+ |
| PUT | `/api/report-schedules/{id}` | Modify schedule | TESTER+ |
| DELETE | `/api/report-schedules/{id}` | Delete schedule | ADMIN/TEST_MANAGER |

**Create Schedule Request Body:**
```json
{
  "reportId": 1,
  "scheduleName": "Daily Executive Report",
  "frequency": "DAILY",
  "timeOfDay": "09:00:00",
  "emailRecipients": "admin@example.com, manager@example.com",
  "isActive": true
}
```

**Other Frequency Options:**
```json
// Weekly - every Monday
{
  "frequency": "WEEKLY",
  "dayOfWeek": 1,
  "timeOfDay": "09:00:00"
}

// Monthly - on 15th
{
  "frequency": "MONTHLY",
  "dayOfMonth": 15,
  "timeOfDay": "09:00:00"
}

// Quarterly - on 1st every 3 months
{
  "frequency": "QUARTERLY",
  "dayOfMonth": 1,
  "timeOfDay": "09:00:00"
}

// Annually - on January 1st
{
  "frequency": "ANNUALLY",
  "dayOfMonth": 1,
  "timeOfDay": "09:00:00"
}
```

### D. Report Executions API (Generate & Download)
**Base Path:** `/api/report-executions`

| Method | Endpoint | Purpose | Auth |
|--------|----------|---------|------|
| GET | `/api/report-executions/report/{reportId}` | List executions for a report | VIEWER+ |
| GET | `/api/report-executions/{id}` | Get execution details | VIEWER+ |
| GET | `/api/report-executions/my-executions` | Get my executed reports | VIEWER+ |
| POST | `/api/report-executions/generate/{reportId}` | Trigger manual report generation | TESTER+ |
| GET | `/api/report-executions/download/{executionId}` | Download CSV report file | VIEWER+ |
| GET | `/api/report-executions/download-list` | List all downloadable reports | VIEWER+ |

**Manual Report Generation:**
```bash
curl -X POST \
  http://localhost:8080/api/report-executions/generate/1 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json"

# Response (202 Accepted - Processing in background)
{
  "id": 45,
  "reportId": 1,
  "reportName": "Daily Test Results",
  "executionType": "MANUAL",
  "status": "PENDING",
  "startTime": "2026-02-18T10:30:00",
  "createdAt": "2026-02-18T10:30:00"
}
```

**Download Report:**
```bash
curl -X GET \
  http://localhost:8080/api/report-executions/download/45 \
  -H "Authorization: Bearer <TOKEN>" \
  -o report_1_20260218_103000_1.csv

# Returns CSV file with data
```

---

## 3. Backend Services (COMPLETED)

### ReportService
- `getAllReports()` - List with pagination and access control
- `createReport()` - Validate and persist report template
- `updateReport()` - Modify existing report
- `deleteReport()` - Remove report and audit
- `searchReports()` - Full-text search by name/description
- `getReportsByView()` - Filter by data source

### ReportViewService
- `getAllViews()` - List available data sources
- `getViewById()` - Get view with all field definitions
- `getViewFields()` - Get all columns for a view
- `getFilterableFields()` - Get only filterable columns
- `getSortableFields()` - Get only sortable columns

### ReportScheduleService
- `createSchedule()` - Create schedule with auto-calculated next_execution
- `updateSchedule()` - Modify schedule and recalculate timing
- `deleteSchedule()` - Remove schedule
- `getPendingSchedules()` - Find schedules ready to execute
- **Smart Frequency Calculation:**
  - DAILY: Tomorrow at specified time
  - WEEKLY: Next occurrence of specified day
  - MONTHLY/QUARTERLY/ANNUALLY: On specified day with lookahead

### ReportGenerationService
- `generateCsvReport()` - Core CSV generation engine
  - Parses selectedColumns JSON
  - Applies filter conditions dynamically
  - Implements sorting configuration
  - Streams large datasets efficiently
  - Escapes CSV special characters
  - Returns file path, size, row count

### ReportExecutionService
- `generateReport()` - Trigger manual report (returns 202 Accepted)
- `generateScheduledReport()` - Execute from schedule
- `generateReportAsync()` - Background async execution
- `getExecutionsByReport()` - Retrieve execution history
- `getMyExecutions()` - User-specific execution history

---

## 4. Frontend Components (TO BE BUILT)

### Components Needed

#### 1. ReportBuilderView
**Purpose:** Create/modify report templates

**Features:**
- Step 1: Select Data Source (dropdown of report_views)
- Step 2: Select Columns (multi-select from report_view_fields)
- Step 3: Add Filters (dynamic based on filterable fields)
  - Field selector → Operator (=, <, >, LIKE) → Value
  - Add multiple filter groups with AND/OR logic
- Step 4: Configure Sorting (drag-drop multi-field sorting)
- Step 5: Preview (show first 10 rows before saving)
- Save button → POST /api/reports

**Form Elements:**
```vue
<template>
  <div class="report-builder">
    <!-- Step 1: View Selection -->
    <select v-model="selectedViewId" @change="loadViewFields">
      <option v-for="view in views" :value="view.id">{{ view.displayName }}</option>
    </select>
    
    <!-- Step 2: Column Selection -->
    <div class="column-selector">
      <label v-for="field in viewFields" :key="field.id">
        <input type="checkbox" v-model="selectedColumns" :value="field.fieldName">
        {{ field.displayName }}
      </label>
    </div>
    
    <!-- Step 3: Filters -->
    <div class="filter-builder">
      <div v-for="(filter, idx) in filters" :key="idx">
        <select v-model="filter.field">
          <option v-for="field in filterableFields" :value="field.fieldName">
            {{ field.displayName }}
          </option>
        </select>
        <select v-model="filter.operator">
          <option>=</option>
          <option>&lt;</option>
          <option>&gt;</option>
          <option>LIKE</option>
        </select>
        <input v-model="filter.value" type="text">
      </div>
      <button @click="addFilter">+ Add Filter</button>
    </div>
    
    <!-- Step 4: Sorting -->
    <div class="sort-builder">
      <div v-for="(sort, idx) in sortConfig" :key="idx">
        <select v-model="sort.field">
          <option v-for="field in sortableFields" :value="field.fieldName">
            {{ field.displayName }}
          </option>
        </select>
        <select v-model="sort.direction">
          <option>ASC</option>
          <option>DESC</option>
        </select>
      </div>
    </div>
    
    <!-- Save -->
    <button @click="saveReport">Save Report Template</button>
  </div>
</template>
```

#### 2. ReportSchedulerView
**Purpose:** Create/manage report schedules

**Features:**
- Select existing report template
- Set frequency (DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY)
- Time picker (HH:mm)
- For WEEKLY: Day of week selector
- For MONTHLY/QUARTERLY/ANNUALLY: Day of month selector
- Email recipients (comma-separated list)
- Active toggle
- Save → POST /api/report-schedules

**List Schedules:**
- Show all with status (active/inactive)
- Edit, Delete, View Last Execution
- Next Execution timestamp

#### 3. ReportHistoryView
**Purpose:** Show all generated reports for download

**Features:**
- Table with columns:
  - Report Name
  - Execution Type (MANUAL/SCHEDULED)
  - Status (PENDING/GENERATING/COMPLETED/FAILED)
  - Generated Date/Time
  - File Size
  - Row Count
  - Download Link
  - Error Message (if failed)

**Filters:**
- Date range picker
- Status filter
- Report name filter
- Execution type filter

**Actions:**
- Download CSV button
- View details modal
- Delete old reports

#### 4. ReportViewComponent
**Purpose:** Display report metadata and available columns

**Used by:** ReportBuilder when selecting data source

---

## 5. Unix/OS Scripts (TO BE BUILT)

### Scheduled Report Execution Script

**Purpose:** Background job to execute scheduled reports at their next_execution time

####  Option 1: PowerShell Script (Windows)
File: `backend/scripts/execute-scheduled-reports.ps1`

```powershell
# Configuration
$ApiBaseUrl = "http://localhost:8080/api"
$Token = "" # Load from secure location
$CheckInterval = 60 # seconds

while ($true) {
    try {
        # Get pending schedules
        $schedules = Invoke-RestMethod `
            -Uri "$ApiBaseUrl/report-schedules" `
            -Method GET `
            -Headers @{ "Authorization" = "Bearer $Token" }
        
        foreach ($schedule in $schedules.content) {
            if ($schedule.isActive -and $schedule.nextExecution -le (Get-Date)) {
                # Execute the scheduled report
                $result = Invoke-RestMethod `
                    -Uri "$ApiBaseUrl/report-executions/generate/$($schedule.reportId)" `
                    -Method POST `
                    -Headers @{ "Authorization" = "Bearer $Token" }
                
                Write-Host "Executed schedule $($schedule.id): $($result.id)"
                
                # Send email if configured
                if ($schedule.emailRecipients) {
                    Send-EmailReport $result.id $schedule.emailRecipients
                }
            }
        }
        
        Start-Sleep -Seconds $CheckInterval
    }
    catch {
        Write-Error "Error executing schedules: $_"
        Start-Sleep -Seconds $CheckInterval
    }
}
```

#### Option 2: Bash Script (Linux/macOS)
File: `backend/scripts/execute-scheduled-reports.sh`

```bash
#!/bin/bash

API_BASE_URL="http://localhost:8080/api"
TOKEN=""  # Load from environment or file
CHECK_INTERVAL=60

while true; do
    # Get pending schedules
    SCHEDULES=$(curl -s \
        -H "Authorization: Bearer $TOKEN" \
        "$API_BASE_URL/report-schedules")
    
    # Process each schedule
    echo "$SCHEDULES" | jq -r '.content[] | select(.isActive and .nextExecution <= now) | .id' | while read SCHEDULE_ID; do
        # Execute report
        RESULT=$(curl -s -X POST \
            -H "Authorization: Bearer $TOKEN" \
            "$API_BASE_URL/report-executions/generate/$SCHEDULE_ID")
        
        echo "Executed schedule: $RESULT"
    done
    
    sleep $CHECK_INTERVAL
done
```

#### Option 3: Spring Boot Scheduled Job (Recommended)
File: `backend/src/main/java/com/kkvat/automation/scheduler/ReportScheduler.java`

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {
    private final ReportScheduleRepository scheduleRepository;
    private final ReportExecutionService executionService;
    private final EmailService emailService;
    
    @Scheduled(fixedDelay = 60000) // Check every 60 seconds
    public void executeScheduledReports() {
        try {
            List<ReportSchedule> pendingSchedules = 
                scheduleRepository.findByIsActiveTrueAndNextExecutionBefore(LocalDateTime.now());
            
            for (ReportSchedule schedule : pendingSchedules) {
                try {
                    // Execute the report
                    ReportExecutionResponse execution = 
                        executionService.generateScheduledReport(schedule.getId());
                    
                    // Send email notification
                    if (schedule.getEmailRecipients() != null && !schedule.getEmailRecipients().isEmpty()) {
                        emailService.sendReportEmail(
                            execution.getReportName(),
                            schedule.getEmailRecipients(),
                            execution.getFilePath()
                        );
                    }
                    
                    log.info("Executed schedule {}: {}", schedule.getId(), execution.getId());
                } catch (Exception e) {
                    log.error("Error executing schedule {}", schedule.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error in report scheduler", e);
        }
    }
}
```

**To Enable:**
- Add `@EnableScheduling` to main application class
- Or configure Quartz for distributed scheduling

---

## 6. Role-Based Permissions (Implemented)

| Role | Rights |
|------|--------|
| **VIEWER** | View reports, download generated reports, view schedules |
| **TESTER** | Create reports, update reports, create schedules, update schedules, generate reports |
| **TEST_MANAGER** | + Delete reports, delete schedules |
| **ADMIN** | All operations |

---

## 7. Testing Phase 4

### API Testing Examples

**1. Create a Report Template:**
```bash
curl -X POST http://localhost:8080/api/reports \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Daily Test Summary",
    "viewId": 1,
    "selectedColumns": ["id", "status", "duration_ms"],
    "reportType": "EXECUTION"
  }'
```

**2. Create a Schedule:**
```bash
curl -X POST http://localhost:8080/api/report-schedules \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "reportId": 1,
    "scheduleName": "Daily 9AM Report",
    "frequency": "DAILY",
    "timeOfDay": "09:00:00",
    "emailRecipients": "admin@example.com",
    "isActive": true
  }'
```

**3. Generate Report Manually:**
```bash
curl -X POST http://localhost:8080/api/report-executions/generate/1 \
  -H "Authorization: Bearer <TOKEN>"

# Get status
curl -X GET http://localhost:8080/api/report-executions/report/1 \
  -H "Authorization: Bearer <TOKEN>"

# Download when COMPLETED
curl -X GET http://localhost:8080/api/report-executions/download/<EXEC_ID> \
  -H "Authorization: Bearer <TOKEN>" \
  -o report.csv
```

---

## 8. Deployment Checklist

- [ ] Update pom.xml with report output directory property
- [ ] Create `/reports` directory on server with appropriate permissions
- [ ] Enable scheduled jobs in application.yml
- [ ] Deploy backend JAR
- [ ] Populate initial report_views and report_view_fields
- [ ] Deploy frontend components
- [ ] Configure scheduled report execution (Spring job or external script)
- [ ] Test complete workflow: Create report → Schedule → Execute → Download
- [ ] Set up email service for schedule notifications
- [ ] Configure backup for generated reports

---

## 9. Configuration

**application.yml additions:**
```yaml
report:
  output:
    directory: ./reports  # Directory to store generated CSV files
    retention-days: 90    # Auto-delete reports older than 90 days

spring:
  scheduling:
    pool:
      size: 5
    thread-name-prefix: report-
  task:
    scheduling:
      enabled: true
```

---

## Next Steps

1. **Frontend Development**
   - Build React/Vue components for Report Builder, Scheduler, History
   - Integrate with backend APIs
   - Add real-time progress tracking for report generation

2. **Advanced Features**
   - Email report delivery with inline CSV
   - Report caching for frequently accessed reports
   - Multi-format export (PDF, Excel in addition to CSV)
   - Report sharing and permissions per-report
   - Scheduled report dashboards

3. **Performance Optimization**
   - Implement pagination for large result sets
   - Add indexing for frequently filtered columns
   - Cache report_views metadata in Redis
   - Implement background job queuing (RabbitMQ/Kafka)

4. **Monitoring & Maintenance**
   - Track report generation metrics
   - Alert on failed scheduled executions
   - Audit trail for report access
   - Disk space monitoring for report storage

---

**Progress**: Phase 4 Backend ✅ Complete | Phase 4 Frontend 🔄 In Progress
**Build Status**: ✅ Successful
**Next Phase**: Phase 5 - Advanced Features & UI Integration
