import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReportSchedule, ReportScheduleRequest, PagedResponse } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportScheduleService {
  private apiUrl = '/api/report-schedules';

  constructor(private http: HttpClient) {}

  getAllSchedules(page: number = 0, size: number = 10): Observable<PagedResponse<ReportSchedule>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<ReportSchedule>>(this.apiUrl, { params });
  }

  getScheduleById(id: number): Observable<ReportSchedule> {
    return this.http.get<ReportSchedule>(`${this.apiUrl}/${id}`);
  }

  createSchedule(schedule: ReportScheduleRequest): Observable<ReportSchedule> {
    return this.http.post<ReportSchedule>(this.apiUrl, schedule);
  }

  updateSchedule(id: number, schedule: ReportScheduleRequest): Observable<ReportSchedule> {
    return this.http.put<ReportSchedule>(`${this.apiUrl}/${id}`, schedule);
  }

  deleteSchedule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getSchedulesByReport(reportId: number): Observable<ReportSchedule[]> {
    return this.http.get<ReportSchedule[]>(`${this.apiUrl}?reportId=${reportId}`);
  }
}
