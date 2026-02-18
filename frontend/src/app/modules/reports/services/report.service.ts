import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ReportView,
  ReportViewField,
  Report,
  ReportRequest,
  ReportResponse,
  ReportSchedule,
  ReportScheduleRequest,
  ReportExecution,
  ReportExecutionResponse,
  PagedResponse
} from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = '/api/reports';

  constructor(private http: HttpClient) {}

  // Report CRUD Operations
  getAllReports(page: number = 0, size: number = 10): Observable<PagedResponse<ReportResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<ReportResponse>>(this.apiUrl, { params });
  }

  getReportById(id: number): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(`${this.apiUrl}/${id}`);
  }

  createReport(report: ReportRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(this.apiUrl, report);
  }

  updateReport(id: number, report: ReportRequest): Observable<ReportResponse> {
    return this.http.put<ReportResponse>(`${this.apiUrl}/${id}`, report);
  }

  deleteReport(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchReports(keyword: string, page: number = 0, size: number = 10): Observable<PagedResponse<ReportResponse>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<ReportResponse>>(`${this.apiUrl}/search`, { params });
  }

  getReportsByView(viewId: number, page: number = 0, size: number = 10): Observable<PagedResponse<ReportResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<ReportResponse>>(`${this.apiUrl}/view/${viewId}`, { params });
  }
}
