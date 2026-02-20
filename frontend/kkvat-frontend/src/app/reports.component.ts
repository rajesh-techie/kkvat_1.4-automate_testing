import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface ReportDto {
  id?: number;
  name: string;
  description?: string;
  viewId?: number;
  reportType?: string;
  isPublic?: boolean;
  selectedColumns?: string[];
  filterConditions?: any;
  sortConfig?: any;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="entity-page">
      <h2>Reports</h2>

      <form (ngSubmit)="create()" class="entity-form">
        <div class="form-row"><label>Name:</label><input [(ngModel)]="model.name" name="name" required /></div>
        <div class="form-row"><label>Description:</label><input [(ngModel)]="model.description" name="description" /></div>
        <div class="form-row"><label>View:</label>
          <select [(ngModel)]="model.viewId" name="viewId" (change)="onViewChange(model.viewId)">
            <option value="">-- Select view --</option>
            <option *ngFor="let v of views" [value]="v.id">{{v.name}}</option>
          </select>
        </div>
        <div class="form-row"><label>Type:</label><input [(ngModel)]="model.reportType" name="reportType" /></div>
        <div class="form-row"><label>Public:</label><input type="checkbox" [(ngModel)]="model.isPublic" name="isPublic" /></div>

        <div class="form-row full-row inline-controls">
          <div class="inline-item">
            <label>Columns:</label>
            <select multiple [(ngModel)]="model.selectedColumns" name="selectedColumns">
              <option *ngFor="let f of allFields" [value]="f.columnName">{{f.columnName}}</option>
            </select>
          </div>

          <div class="inline-item">
            <label>Filters:</label>
            <select multiple [(ngModel)]="model.filterConditions" name="filteredConditions">
              <option *ngFor="let f of filterableFields" [value]="f.fieldName">{{f.fieldName}}</option>
            </select>
          </div>

          <div class="inline-item">
            <label>Sort:</label>
            <select multiple [(ngModel)]="model.sortConfig" name="sortConfig">
              <option *ngFor="let f of sortableFields" [value]="f.fieldName">{{f.fieldName}}</option>
            </select>
          </div>
        </div>

        <div class="form-row full-row"><button type="submit">{{model.id ? 'Save' : 'Create'}}</button><button type="button" (click)="clearCreate()">Cancel</button></div>
      </form>

      <div class="search-bar">
        <input [(ngModel)]="searchQuery" name="search" placeholder="Search by name or description" />
        <button type="button" (click)="search()">Search</button>
        <button type="button" (click)="clearSearch()">Clear</button>
      </div>

      <table *ngIf="reports?.length">
        <thead><tr><th>Name</th><th>Description</th><th>Type</th><th>Public</th><th>Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let r of reports">
            <td>{{r.name}}</td>
            <td>{{r.description}}</td>
            <td>{{r.reportType}}</td>
            <td>{{r.isPublic ? 'Yes' : 'No'}}</td>
            <td>
              <button (click)="editReport(r)">Edit</button>
              <button (click)="deleteReport(r.id)">Delete</button>
              <button (click)="generate(r.id)">Generate</button>
            </td>
          </tr>
        </tbody>
      </table>

      <p *ngIf="!reports || reports.length === 0">No reports found.</p>

      <div class="pagination" *ngIf="!isSearching && totalPages > 1">
        <button [disabled]="page <= 0" (click)="loadReports(page - 1)">Previous</button>
        <button *ngFor="let p of [].constructor(totalPages); let i = index" [disabled]="i === page" (click)="loadReports(i)">{{i + 1}}</button>
        <button [disabled]="page >= totalPages - 1" (click)="loadReports(page + 1)">Next</button>
        <span class="pager-summary">Page {{page + 1}} of {{totalPages}} — Total: {{totalElements}}</span>
      </div>
    </div>
  `,
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  reports: ReportDto[] = [];
  error: string | null = null;

  // single model used for both create and edit
  model: ReportDto = { name: '', description: '', isPublic: false, selectedColumns: [], filterConditions: [], sortConfig: [] };

  // pagination and search
  searchQuery = '';
  api = 'http://localhost:8080/api/reports';
  page = 0;
  size = 6;
  totalPages = 0;
  totalElements = 0;
  isSearching = false;

  // views and fields
  views: any[] = [];
  allFields: any[] = [];
  filterableFields: any[] = [];
  sortableFields: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadReports(0);
    this.loadViews();
  }

  loadViews() {
    this.http.get<any[]>('http://localhost:8080/api/report-views').subscribe(vs => this.views = vs || []);
  }

  loadReports(page: number = 0) {
    this.error = null;
    this.isSearching = false;
    this.page = page;
    this.http.get<any>(`${this.api}?page=${page}&size=${this.size}`).subscribe(
      res => {
        this.reports = (res && res.content) || (res && res.data) || res || [];
        this.totalPages = (res && res.totalPages) || 0;
        this.totalElements = (res && res.totalElements) || 0;
      },
      err => this.error = 'Failed to load reports'
    );
  }

  create() {
    this.error = null;
    const payload = { ...this.model } as ReportDto;
    if (this.model.id) {
      this.http.put<any>(`${this.api}/${this.model.id}`, payload).subscribe(() => {
        this.clearCreate();
        this.loadReports(this.page);
      }, e => this.error = 'Failed to update report');
    } else {
      this.http.post<any>(this.api, payload).subscribe(() => {
        this.clearCreate();
        this.loadReports(0);
      }, e => this.error = 'Failed to create report');
    }
  }

  clearCreate() {
    this.model = { name: '', description: '', isPublic: false, selectedColumns: [], filterConditions: [], sortConfig: [] };
  }

  search() {
    const q = (this.searchQuery || '').trim();
    if (!q) { this.loadReports(0); return; }
    this.isSearching = true;
    this.http.get<any>(`${this.api}/search?keyword=${encodeURIComponent(q)}&page=0&size=${this.size}`).subscribe(
      res => {
        this.reports = (res && res.content) || (res && res.data) || res || [];
        this.totalPages = (res && res.totalPages) || 0;
        this.totalElements = (res && res.totalElements) || this.reports.length;
      },
      err => this.error = 'Search failed'
    );
  }

  clearSearch() { this.searchQuery = ''; this.loadReports(0); }

  newReport() { this.clearCreate(); }

  editReport(r: ReportDto) {
    this.model = { ...r };
    this.model.selectedColumns = this.model.selectedColumns || [];
    this.model.filterConditions = this.model.filterConditions || [];
    this.model.sortConfig = this.model.sortConfig || [];
    if (this.model.viewId) { this.onViewChange(this.model.viewId); }
  }

  onViewChange(viewId?: number) {
    if (!viewId) { this.allFields = []; this.filterableFields = []; this.sortableFields = []; return; }
    const schema = 'kkvat_automation';
    this.http.get<any[]>(`http://localhost:8080/api/report-views/${viewId}/columns?schema=${encodeURIComponent(schema)}`).subscribe(f => this.allFields = f || []);
    this.http.get<any[]>(`http://localhost:8080/api/report-views/${viewId}/fields/filterable`).subscribe(f => this.filterableFields = f || []);
    this.http.get<any[]>(`http://localhost:8080/api/report-views/${viewId}/fields/sortable`).subscribe(f => this.sortableFields = f || []);
  }

  cancelEdit() { this.clearCreate(); this.error = null; }

  deleteReport(id?: number) {
    if (!id || !confirm('Delete this report?')) return;
    this.http.delete<any>(`${this.api}/${id}`).subscribe(() => this.loadReports(this.page), e => alert('Failed to delete'));
  }

  generate(id?: number) {
    if (!id) return;
    this.http.post<any>(`http://localhost:8080/api/report-executions/generate/${id}`, {}).subscribe(() => alert('Report generation requested'), () => alert('Error'));
  }
}
