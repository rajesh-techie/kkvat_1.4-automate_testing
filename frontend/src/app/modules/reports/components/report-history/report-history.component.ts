import { Component, OnInit } from '@angular/core';
import { ReportExecutionService } from '../../services/report-execution.service';
import { ReportExecutionResponse } from '../../models/report.model';

@Component({
  selector: 'app-report-history',
  templateUrl: './report-history.component.html',
  styleUrls: ['./report-history.component.css']
})
export class ReportHistoryComponent implements OnInit {
  executions: ReportExecutionResponse[] = [];
  
  // Filters
  statusFilter = 'ALL';
  executionTypeFilter = 'ALL';
  dateFrom: string | null = null;
  dateTo: string | null = null;
  
  // UI State
  loading = false;
  downloading: Set<number> = new Set();
  error: string | null = null;
  success: string | null = null;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalExecutions = 0;
  
  // Status and execution type options
  statuses = ['ALL', 'PENDING', 'GENERATING', 'COMPLETED', 'FAILED'];
  executionTypes = ['ALL', 'MANUAL', 'SCHEDULED', 'API'];

  constructor(private executionService: ReportExecutionService) {}

  ngOnInit(): void {
    this.loadExecutions();
  }

  loadExecutions(): void {
    this.loading = true;
    this.executionService.getDownloadableReports(this.currentPage, this.pageSize).subscribe(
      (response) => {
        let executions = response.content;
        
        // Apply filters
        if (this.statusFilter !== 'ALL') {
          executions = executions.filter(e => e.status === this.statusFilter);
        }
        if (this.executionTypeFilter !== 'ALL') {
          executions = executions.filter(e => e.executionType === this.executionTypeFilter);
        }
        if (this.dateFrom) {
          executions = executions.filter(e => new Date(e.createdAt) >= new Date(this.dateFrom!));
        }
        if (this.dateTo) {
          executions = executions.filter(e => new Date(e.createdAt) <= new Date(this.dateTo!));
        }
        
        this.executions = executions;
        this.totalExecutions = response.totalElements;
        this.loading = false;
      },
      (error) => {
        this.error = 'Failed to load execution history';
        this.loading = false;
      }
    );
  }

  downloadReport(execution: ReportExecutionResponse): void {
    if (execution.status !== 'COMPLETED') {
      this.error = 'Report is not ready for download';
      return;
    }

    this.downloading.add(execution.id);
    const filename = `${execution.reportName}_${new Date(execution.createdAt).toISOString().split('T')[0]}.csv`;
    this.executionService.downloadFile(execution.id, filename);
    
    setTimeout(() => {
      this.downloading.delete(execution.id);
      this.success = 'Report downloaded successfully!';
    }, 1000);
  }

  isDownloadable(execution: ReportExecutionResponse): boolean {
    return execution.status === 'COMPLETED' && execution.filePath !== null;
  }

  getStatusBadgeClass(status: string): string {
    const classes: any = {
      'PENDING': 'badge-warning',
      'GENERATING': 'badge-info',
      'COMPLETED': 'badge-success',
      'FAILED': 'badge-danger'
    };
    return classes[status] || 'badge-secondary';
  }

  getStatusDisplay(status: string): string {
    const displays: any = {
      'PENDING': '⏱ Pending',
      'GENERATING': '⚙ Generating',
      'COMPLETED': '✓ Completed',
      'FAILED': '✗ Failed'
    };
    return displays[status] || status;
  }

  getExecutionTypeDisplay(type: string): string {
    const displays: any = {
      'MANUAL': 'Manual',
      'SCHEDULED': 'Scheduled',
      'API': 'API Call'
    };
    return displays[type] || type;
  }

  formatDate(date: Date | string): string {
    return new Date(date).toLocaleString();
  }

  formatDuration(durationMs: number | undefined): string {
    if (!durationMs) return '-';
    const seconds = Math.floor(durationMs / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes % 60}m`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds % 60}s`;
    } else {
      return `${seconds}s`;
    }
  }

  formatFileSize(bytes: number | undefined): string {
    if (!bytes) return '-';
    const sizes = ['B', 'KB', 'MB', 'GB'];
    if (bytes === 0) return '0 B';
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round((bytes / Math.pow(1024, i)) * 100) / 100 + ' ' + sizes[i];
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadExecutions();
  }

  resetFilters(): void {
    this.statusFilter = 'ALL';
    this.executionTypeFilter = 'ALL';
    this.dateFrom = null;
    this.dateTo = null;
    this.currentPage = 0;
    this.loadExecutions();
  }

  nextPage(): void {
    if (this.currentPage < Math.ceil(this.totalExecutions / this.pageSize) - 1) {
      this.currentPage++;
      this.loadExecutions();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadExecutions();
    }
  }

  get hasNextPage(): boolean {
    return this.currentPage < Math.ceil(this.totalExecutions / this.pageSize) - 1;
  }

  get hasPreviousPage(): boolean {
    return this.currentPage > 0;
  }

  clearAlert(): void {
    this.error = null;
    this.success = null;
  }
}
