import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReportExecution, ReportExecutionResponse, PagedResponse } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportExecutionService {
  private apiUrl = '/api/report-executions';

  constructor(private http: HttpClient) {}

  getExecutionsByReport(reportId: number, page: number = 0, size: number = 10): Observable<PagedResponse<ReportExecutionResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<ReportExecutionResponse>>(`${this.apiUrl}/report/${reportId}`, { params });
  }

  getExecutionById(id: number): Observable<ReportExecutionResponse> {
    return this.http.get<ReportExecutionResponse>(`${this.apiUrl}/${id}`);
  }

  getMyExecutions(page: number = 0, size: number = 10): Observable<PagedResponse<ReportExecutionResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<ReportExecutionResponse>>(`${this.apiUrl}/my-executions`, { params });
  }

  generateReport(reportId: number): Observable<ReportExecutionResponse> {
    return this.http.post<ReportExecutionResponse>(`${this.apiUrl}/generate/${reportId}`, {});
  }

  downloadReport(executionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/${executionId}`, {
      responseType: 'blob'
    });
  }

  getDownloadableReports(page: number = 0, size: number = 10): Observable<PagedResponse<ReportExecutionResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<ReportExecutionResponse>>(`${this.apiUrl}/download-list`, { params });
  }

  // Utility method to trigger download
  downloadFile(executionId: number, filename: string): void {
    this.downloadReport(executionId).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename || `report_${executionId}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    });
  }
}
