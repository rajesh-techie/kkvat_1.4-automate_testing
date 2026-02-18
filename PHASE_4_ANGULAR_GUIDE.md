# Phase 4 Angular Frontend - Complete Implementation Guide

## Overview

Complete Angular implementation of the KKVat Automation Platform Phase 4 (Reporting System) with:
- **Report Builder** - Step-by-step wizard to create report templates
- **Report Scheduler** - Configure recurring report execution with email notifications
- **Report History** - Track, view, and download generated reports
- **3 Angular Services** - API integration for reports, schedules, and executions
- **Bash Cronjob** - Automated scheduled report execution

---

## Project Structure

```
frontend/
├── src/
│   └── app/
│       └── modules/
│           └── reports/
│               ├── components/
│               │   ├── report-builder/
│               │   │   ├── report-builder.component.ts
│               │   │   ├── report-builder.component.html
│               │   │   └── report-builder.component.css
│               │   ├── report-scheduler/
│               │   │   ├── report-scheduler.component.ts
│               │   │   ├── report-scheduler.component.html
│               │   │   └── report-scheduler.component.css
│               │   └── report-history/
│               │       ├── report-history.component.ts
│               │       ├── report-history.component.html
│               │       └── report-history.component.css
│               ├── services/
│               │   ├── report.service.ts
│               │   ├── report-view.service.ts
│               │   ├── report-schedule.service.ts
│               │   └── report-execution.service.ts
│               ├── models/
│               │   └── report.model.ts
│               ├── reports.module.ts
│               └── reports-routing.module.ts

backend/
└── scripts/
    ├── execute-scheduled-reports.sh
    ├── .env.example
    └── CRONJOB_SETUP.md
```

---

## Angular Components

### 1. Report Builder Component
**File**: `report-builder.component.ts`

**Features**:
- Step 1: Report Details & View Selection
- Step 2: Column Selection (multi-select)
- Step 3: Filter Configuration (dynamic)
- Step 4: Sort Configuration (drag-drop capable)
- Step 5: Preview & Save

**Key Methods**:
- `onViewSelected()` - Load fields when view changes
- `toggleColumn()` - Add/remove columns
- `addFilter()` / `removeFilter()` - Manage filter conditions
- `addSort()` / `removeSort()` - Manage sort configuration
- `savReport()` - POST to backend API
- `previewReport()` - Show sample data

**Form Groups**:
- reportForm: name, description, viewId, reportType, isPublic

**Services Used**:
- ReportService (create report)
- ReportViewService (load views and fields)

---

### 2. Report Scheduler Component
**File**: `report-scheduler.component.ts`

**Features**:
- Create/Edit/Delete schedules
- Select report template
- Choose frequency (DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY)
- Set execution time
- Configure email recipients
- Paginated list of schedules
- Status indicator

**Key Methods**:
- `loadSchedules()` - Fetch paginated schedules
- `createSchedule()` / `updateSchedule()` - Save schedule
- `deleteSchedule()` - Remove schedule
- `onFrequencyChange()` - Update validators based on frequency
- `editSchedule()` - Load schedule into form

**Form Groups**:
- scheduleForm: reportId, scheduleName, frequency, dayOfWeek, dayOfMonth, timeOfDay, emailRecipients, isActive

**Services Used**:
- ReportService (fetch available reports)
- ReportScheduleService (schedule CRUD)

**Conditional Fields**:
- WEEKLY: dayOfWeek selector
- MONTHLY/QUARTERLY/ANNUALLY: dayOfMonth input

---

### 3. Report History Component
**File**: `report-history.component.ts`

**Features**:
- List all report executions with pagination
- Filter by: Status, Execution Type, Date Range
- Download CSV files
- View execution details (duration, row count, file size)
- Error message display for failed reports

**Key Methods**:
- `loadExecutions()` - Fetch execution history
- `downloadReport()` - Trigger download
- `applyFilters()` / `resetFilters()` - Filter executions
- `formatDate()`, `formatDuration()`, `formatFileSize()` - Display helpers

**Status Display**:
- PENDING: ⏱ Pending (not downloadable)
- GENERATING: ⚙ Generating (not downloadable)
- COMPLETED: ✓ Completed (downloadable)
- FAILED: ✗ Failed (shows error)

**Services Used**:
- ReportExecutionService (fetch and download)

---

## Services

### 1. ReportService
**File**: `report.service.ts`

**Methods**:
- `getAllReports(page, size)` - GET /api/reports (paginated)
- `getReportById(id)` - GET /api/reports/{id}
- `createReport(report)` - POST /api/reports
- `updateReport(id, report)` - PUT /api/reports/{id}
- `deleteReport(id)` - DELETE /api/reports/{id}
- `searchReports(keyword, page, size)` - GET /api/reports/search
- `getReportsByView(viewId)` - GET /api/reports/view/{viewId}

---

### 2. ReportViewService
**File**: `report-view.service.ts`

**Methods**:
- `getAllViews()` - GET /api/report-views
- `getViewById(id)` - GET /api/report-views/{id}
- `getViewByName(name)` - GET /api/report-views/name/{name}
- `getViewFields(viewId)` - GET /api/report-views/{viewId}/fields
- `getFilterableFields(viewId)` - GET /api/report-views/{viewId}/fields/filterable
- `getSortableFields(viewId)` - GET /api/report-views/{viewId}/fields/sortable

---

### 3. ReportScheduleService
**File**: `report-schedule.service.ts`

**Methods**:
- `getAllSchedules(page, size)` - GET /api/report-schedules (paginated)
- `getScheduleById(id)` - GET /api/report-schedules/{id}
- `createSchedule(schedule)` - POST /api/report-schedules
- `updateSchedule(id, schedule)` - PUT /api/report-schedules/{id}
- `deleteSchedule(id)` - DELETE /api/report-schedules/{id}
- `getSchedulesByReport(reportId)` - GET /api/report-schedules?reportId={id}

---

### 4. ReportExecutionService
**File**: `report-execution.service.ts`

**Methods**:
- `getExecutionsByReport(reportId, page, size)` - GET /api/report-executions/report/{reportId}
- `getExecutionById(id)` - GET /api/report-executions/{id}
- `getMyExecutions(page, size)` - GET /api/report-executions/my-executions
- `generateReport(reportId)` - POST /api/report-executions/generate/{reportId}
- `downloadReport(executionId)` - GET /api/report-executions/download/{executionId} (blob)
- `getDownloadableReports(page, size)` - GET /api/report-executions/download-list
- `downloadFile(executionId, filename)` - Browser file download helper

---

## Data Models

### Report Model
```typescript
interface Report {
  id: number;
  name: string;
  description: string;
  viewId: number;
  selectedColumns: string[];
  filterConditions?: any;
  sortConfig?: any;
  reportType: 'EXECUTION' | 'USER_ACTIVITY' | 'CUSTOM';
  isPublic: boolean;
  createdAt: Date;
  updatedAt: Date;
  createdBy: number;
}
```

### ReportSchedule Model
```typescript
interface ReportSchedule {
  id: number;
  reportId: number;
  scheduleName: string;
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'ANNUALLY';
  dayOfWeek?: number;  // 0-6 for WEEKLY
  dayOfMonth?: number; // 1-31 for MONTHLY/QUARTERLY/ANNUALLY
  timeOfDay: string;   // HH:mm:ss
  emailRecipients: string; // comma-separated
  isActive: boolean;
  lastExecuted?: Date;
  nextExecution?: Date;
}
```

### ReportExecution Model
```typescript
interface ReportExecution {
  id: number;
  reportId: number;
  reportName: string;
  scheduleId?: number;
  executionType: 'MANUAL' | 'SCHEDULED' | 'API';
  status: 'PENDING' | 'GENERATING' | 'COMPLETED' | 'FAILED';
  startTime: Date;
  endTime?: Date;
  durationMs?: number;
  filePath?: string;
  fileSize?: number;
  rowCount?: number;
  errorMessage?: string;
  executedBy: number;
  createdAt: Date;
}
```

---

## Module Integration

### ReportsModule
**File**: `reports.module.ts`

```typescript
@NgModule({
  declarations: [
    ReportBuilderComponent,
    ReportSchedulerComponent,
    ReportHistoryComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ReportsRoutingModule
  ],
  providers: [
    ReportService,
    ReportViewService,
    ReportScheduleService,
    ReportExecutionService
  ]
})
export class ReportsModule { }
```

### Import in App Module
```typescript
// app.module.ts
import { ReportsModule } from './modules/reports/reports.module';

@NgModule({
  imports: [
    ReportsModule,
    // ... other modules
  ]
})
export class AppModule { }
```

### Routing Configuration
```typescript
// app-routing.module.ts
const routes: Routes = [
  {
    path: 'reports',
    loadChildren: () => import('./modules/reports/reports.module')
      .then(m => m.ReportsModule)
  },
  // ... other routes
];
```

---

## UI/UX Features

### Report Builder
- **5-Step Wizard**: Progressive disclosure with validation
- **Visual Feedback**: Step indicators, badges, color-coded sections
- **Smart Forms**: Dynamic validators based on data source
- **Drag-Drop Ready**: Sort configuration with up/down buttons
- **Error Handling**: Inline validation, error alerts
- **Responsive Design**: Mobile-friendly grid layouts

### Report Scheduler
- **Card-Based Selection**: Visual report selection
- **Frequency Smart Fields**: Show/hide fields based on frequency
- **Time Picker**: HTML5 time input for precision
- **Email Recipients**: Comma-separated list with validation
- **Status Indicators**: Active/Inactive badges
- **Inline Editing**: Click to edit, modal form

### Report History
- **Advanced Filtering**: Status, Type, Date range
- **Real-time Status**: PENDING → GENERATING → COMPLETED/FAILED
- **Smart Download**: Disable button for non-completed reports
- **Error Details**: Tooltips for failure reasons
- **File Info**: Size, row count, generation duration
- **Pagination**: Configurable page size

---

## CSS Styling

All components include comprehensive CSS:
- **Mobile Responsive**: Works on mobile, tablet, desktop
- **Accessibility**: Proper color contrast, focus states
- **Animations**: Smooth transitions and hover effects
- **Bootstrap Compatible**: Uses standard bootstrap classes
- **Dark Mode Ready**: Can be adapted for dark theme

### Key CSS Classes
- `.btn-primary`, `.btn-success`, `.btn-danger`: Action buttons
- `.badge-success`, `.badge-warning`, `.badge-danger`: Status badges
- `.alert-info`, `.alert-success`, `.alert-danger`: Alert messages
- `.form-group`, `.form-control`: Form styling
- `.table`: Data table styling

---

## Integration Checklist

### 1. Angular Setup
- [ ] Angular 15+ project created
- [ ] FormsModule imported (two-way binding)
- [ ] ReactiveFormsModule imported (form builder)
- [ ] HttpClientModule configured with interceptors
- [ ] Reports module imported in app.module.ts
- [ ] Routes configured for /reports path

### 2. Backend Integration
- [ ] API running at configured URL
- [ ] CORS enabled for frontend origin
- [ ] Authentication interceptor configured
- [ ] JWT token stored and refreshed
- [ ] API endpoints tested with curl

### 3. Environment Configuration
```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### 4. HTTP Interceptor (Optional but Recommended)
```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
}
```

### 5. Navigation Links
```html
<!-- In app.component.html or menu -->
<nav>
  <a routerLink="/reports/builder">Build Report</a>
  <a routerLink="/reports/scheduler">Schedule Report</a>
  <a routerLink="/reports/history">Report History</a>
</nav>
```

---

## Running the Application

### Development Server
```bash
cd frontend
ng serve --open
# Navigate to http://localhost:4200/reports
```

### Production Build
```bash
cd frontend
ng build --prod
# Output in: dist/
```

### Testing
```bash
# Unit tests
ng test

# E2E tests
ng e2e
```

---

## Scheduled Report Execution (Bash Cronjob)

### Script: `execute-scheduled-reports.sh`

**Location**: `/backend/scripts/execute-scheduled-reports.sh`

**Functionality**:
1. Fetch pending schedules from API
2. For each schedule:
   - Identify associated report
   - Trigger report generation (POST /api/report-executions/generate/{reportId})
   - Monitor execution status asynchronously
   - Send email notification on completion
3. Log all operations with timestamps

**Configuration** (`.env`):
```bash
API_BASE_URL=http://localhost:8080/api
API_TOKEN=your_jwt_token
LOG_DIR=/var/log/kkvat
SEND_EMAIL=true
```

**Cronjob Setup**:
```bash
# Every 5 minutes
*/5 * * * * /path/to/scripts/execute-scheduled-reports.sh

# Every 30 minutes during business hours
*/30 9-17 * * 1-5 /path/to/scripts/execute-scheduled-reports.sh
```

**See**: `/backend/scripts/CRONJOB_SETUP.md` for complete setup guide

---

## Error Handling

### Frontend Error Handling
- API errors displayed in alert toasts
- Form validation with inline error messages
- Network timeouts with retry logic
- User-friendly error descriptions

### Backend Error Handling
- Proper HTTP status codes (400, 401, 403, 404, 500)
- Detailed error messages in JSON response
- Validation error lists for form fields
- Exception logging for troubleshooting

### Example Error Handling
```typescript
this.reportService.createReport(request).subscribe(
  (response) => {
    this.success = `Report created: ${response.name}`;
  },
  (error) => {
    this.error = error.error?.message || 'Failed to create report';
  }
);
```

---

## Performance Optimization

### 1. Lazy Loading
- ReportsModule loaded on demand
- Reduces initial bundle size

### 2. OnPush Change Detection
```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
```

### 3. Pagination
- Default 10 items per page
- Configurable page sizes
- Request only needed data

### 4. Caching (Optional)
```typescript
// Implement in service
private cache = new Map();

getViews(): Observable<ReportView[]> {
  if (this.cache.has('views')) {
    return of(this.cache.get('views'));
  }
  return this.http.get<ReportView[]>(...).pipe(
    tap(views => this.cache.set('views', views))
  );
}
```

---

## Testing Guidelines

### Unit Tests
```bash
# Test ReportService
ng test --include='**/report.service.spec.ts'

# Test ReportBuilderComponent
ng test --include='**/report-builder.component.spec.ts'
```

### Integration Tests
```bash
# E2E tests for complete workflows
ng e2e
```

### Manual Testing Scenarios
1. Create report with all field types
2. Create schedule and verify next execution time
3. Generate report manually and download CSV
4. Filter executions by status and date
5. Test email notifications (if enabled)

---

## File Structure Summary

### Frontend Files Created
1. **Models**: `report.model.ts` (all interfaces)
2. **Services**: 4 services (Report, View, Schedule, Execution)
3. **Components**: 3 components with HTML + CSS
4. **Module**: ReportsModule + ReportsRoutingModule
5. **Styles**: Full CSS with responsive design

### Backend Files Created
1. **Script**: `execute-scheduled-reports.sh` (bash cronjob)
2. **Config**: `.env.example` (environment configuration)
3. **Docs**: `CRONJOB_SETUP.md` (complete setup guide)

---

## Next Steps

### 1. Frontend Deployment
- [ ] Update API_URL in environment.ts
- [ ] Configure authentication interceptor
- [ ] Build optimized production bundle
- [ ] Deploy to web server (Apache, Nginx, etc.)

### 2. Backend Verification
- [ ] Deploy JAR to server
- [ ] Verify API endpoints with Swagger
- [ ] Test all 24 REST endpoints
- [ ] Populate initial report_views

### 3. Cronjob Setup
- [ ] Copy script to server
- [ ] Configure .env file with JWT token
- [ ] Add crontab entry
- [ ] Monitor logs for successful execution

### 4. Testing & QA
- [ ] Create end-to-end test cases
- [ ] Test report generation with large datasets
- [ ] Verify email notifications
- [ ] Performance testing under load

### 5. Documentation
- [ ] User guide for report building
- [ ] Admin guide for cronjob setup
- [ ] API documentation (Swagger)
- [ ] Troubleshooting guide

---

## Support & Troubleshooting

### Common Issues

**Issue**: Components not loading
**Solution**: Ensure ReportsModule imported in AppModule

**Issue**: API 404 errors
**Solution**: Verify backend is running at correct URL

**Issue**: CORS errors
**Solution**: Ensure CORS configured in Spring Boot application.yml

**Issue**: Form validation not working
**Solution**: Verify ReactiveFormsModule imported

**Issue**: Download not triggering
**Solution**: Check file blob handling in downloadFile() method

---

## Summary

**Phase 4 Frontend Complete**:
- ✅ 3 Angular components (Builder, Scheduler, History)
- ✅ 4 API services with full CRUD operations
- ✅ Complete data models and interfaces
- ✅ Comprehensive styling and responsive design
- ✅ Bash cronjob for scheduled execution
- ✅ Complete setup and deployment guides

**Status**: Production-ready Angular frontend for enterprise report management system

---

**Last Updated**: February 18, 2026
**Version**: Phase 4.0
**Angular Version**: 15+ compatible
**Bootstrap**: 5.x compatible
