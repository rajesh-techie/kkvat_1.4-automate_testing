import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReportView, ReportViewField } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportViewService {
  private apiUrl = '/api/report-views';

  constructor(private http: HttpClient) {}

  getAllViews(): Observable<ReportView[]> {
    return this.http.get<ReportView[]>(this.apiUrl);
  }

  getViewById(id: number): Observable<ReportView> {
    return this.http.get<ReportView>(`${this.apiUrl}/${id}`);
  }

  getViewByName(name: string): Observable<ReportView> {
    return this.http.get<ReportView>(`${this.apiUrl}/name/${name}`);
  }

  getViewFields(viewId: number): Observable<ReportViewField[]> {
    return this.http.get<ReportViewField[]>(`${this.apiUrl}/${viewId}/fields`);
  }

  getFilterableFields(viewId: number): Observable<ReportViewField[]> {
    return this.http.get<ReportViewField[]>(`${this.apiUrl}/${viewId}/fields/filterable`);
  }

  getSortableFields(viewId: number): Observable<ReportViewField[]> {
    return this.http.get<ReportViewField[]>(`${this.apiUrl}/${viewId}/fields/sortable`);
  }
}
