// Report Model and Interfaces

export interface ReportView {
  id: number;
  name: string;
  displayName: string;
  description: string;
  tableName: string;
  isActive: boolean;
  fields?: ReportViewField[];
}

export interface ReportViewField {
  id: number;
  viewId: number;
  fieldName: string;
  displayName: string;
  fieldType: string;
  isFilterable: boolean;
  isSortable: boolean;
}

export interface Report {
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
  createdByName?: string;
  updatedBy?: number;
}

export interface ReportRequest {
  name: string;
  description: string;
  viewId: number;
  selectedColumns: string[];
  filterConditions?: any;
  sortConfig?: any;
  reportType: 'EXECUTION' | 'USER_ACTIVITY' | 'CUSTOM';
  isPublic: boolean;
}

export interface ReportResponse {
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
  createdByName: string;
}

export interface ReportSchedule {
  id: number;
  reportId: number;
  scheduleName: string;
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'ANNUALLY';
  dayOfWeek?: number;
  dayOfMonth?: number;
  timeOfDay: string;
  emailRecipients: string;
  isActive: boolean;
  lastExecuted?: Date;
  nextExecution?: Date;
  createdAt: Date;
  updatedAt: Date;
  createdBy: number;
}

export interface ReportScheduleRequest {
  reportId: number;
  scheduleName: string;
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'ANNUALLY';
  dayOfWeek?: number;
  dayOfMonth?: number;
  timeOfDay: string;
  emailRecipients: string;
  isActive: boolean;
}

export interface ReportExecution {
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

export interface ReportExecutionResponse {
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
  createdAt: Date;
}

export interface FilterCondition {
  field: string;
  operator: '=' | '<' | '>' | 'LIKE' | '<=' | '>=';
  value: any;
}

export interface SortConfig {
  field: string;
  direction: 'ASC' | 'DESC';
}

export interface PagedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: any;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}
