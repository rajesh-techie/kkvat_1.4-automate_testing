import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-reports-generator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports-generator.component.html',
  styleUrls: ['./reports-generator.component.css']
})
export class ReportsGeneratorComponent implements OnInit {
  reports: any[] = [];
  selectedReportId?: number | null = null;
  reportDetail: any = null;
  filterableFields: any[] = [];
  filterInputs: Array<{name:string, display:string, type:string, value:any}> = [];
  results: any[] = [];
  api = 'http://localhost:8080/api';
  loading = false;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.http.get<any>(`${this.api}/reports?page=0&size=1000`).subscribe(res => {
      this.reports = (res && res.content) || res || [];
    }, () => this.reports = []);
  }

  onReportSelect(id: number | null) {
    this.selectedReportId = id || null;
    this.reportDetail = null;
    this.filterableFields = [];
    this.filterInputs = [];
    this.results = [];
    if (!id) return;
    this.http.get<any>(`${this.api}/reports/${id}`).subscribe(r => {
      this.reportDetail = r;
      const viewId = r.viewId;
      if (viewId) {
        this.http.get<any[]>(`${this.api}/report-views/${viewId}/fields/filterable`).subscribe(f => {
          this.filterableFields = f || [];
          this.prepareInputsFromReport();
        }, () => { this.filterableFields = []; this.prepareInputsFromReport(); });
      } else {
        this.prepareInputsFromReport();
      }
    }, () => this.error = 'Failed to load report details');
  }

  prepareInputsFromReport() {
    const defaults: any = {};
    let allowedFieldNames: string[] | null = null;

    try {
      const raw = this.reportDetail && this.reportDetail.filterConditions ? this.reportDetail.filterConditions : null;
      const parsed = raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : null;

      // If parsed is an object with keys -> use its keys and capture defaults
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        Object.assign(defaults, parsed);
        allowedFieldNames = Object.keys(parsed);
      }

      // If parsed is an array of strings -> use directly
      if (!allowedFieldNames && Array.isArray(parsed) && parsed.length > 0 && typeof parsed[0] === 'string') {
        allowedFieldNames = parsed as string[];
      }

      // If parsed is an array of objects (e.g. [{ fieldName: 'email' }]) -> extract field names
      if (!allowedFieldNames && Array.isArray(parsed) && parsed.length > 0 && typeof parsed[0] === 'object') {
        allowedFieldNames = (parsed as any[]).map(p => p.fieldName || p.name || p.field).filter(x => !!x);
      }
    } catch (e) {
      // ignore parse failures and fall back
    }

    // If the report response explicitly lists filter fields, create inputs from that response
    if (allowedFieldNames && allowedFieldNames.length > 0) {
      const allowed = allowedFieldNames.map(s => (s || '').toString().trim());
      this.filterInputs = allowed.map(name => {
        // try to find metadata from previously-fetched filterableFields if available
        const meta = this.filterableFields.find(f => ((f.fieldName || '').toString().toLowerCase().trim() === name.toLowerCase().trim()) || ((f.displayName || '').toString().toLowerCase().trim() === name.toLowerCase().trim()));
        const display = meta ? (meta.displayName || meta.fieldName) : this.prettifyFieldName(name);
        const type = meta ? (meta.fieldType || 'string') : 'string';
        const value = defaults[name] ?? defaults[this.normalizeKey(name)] ?? '';
        return { name, display, type, value } as {name:string, display:string, type:string, value:any};
      });
      return;
    }

    // Fallback: if no explicit filterConditions from report, use filterableFields from view
    this.filterInputs = this.filterableFields.map(f => ({ name: f.fieldName, display: f.displayName || f.fieldName, type: f.fieldType || 'string', value: '' }));
  }

  private prettifyFieldName(name: string) {
    if (!name) return name;
    // convert camelCase or snake_case to Title Case
    const s = name.replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/[_\-]+/g, ' ');
    return s.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
  }

  private normalizeKey(k: string) {
    return (k || '').toString().toLowerCase().trim();
  }

  generate() {
    if (!this.selectedReportId) return;
    const overrides: any = {};
    this.filterInputs.forEach(fi => {
      if (fi.value !== null && fi.value !== '') overrides[fi.name] = fi.value;
    });
    this.loading = true;
    this.error = null;
    const body = {
      select_columns: this.reportDetail && this.reportDetail.selectedColumns ? this.reportDetail.selectedColumns : undefined,
      view_id: this.reportDetail && this.reportDetail.viewId ? this.reportDetail.viewId : undefined,
      filter_condition: overrides
    };

    this.http.post<any[]>(`${this.api}/report-executions/run/${this.selectedReportId}`, body).subscribe(res => {
      this.results = res || [];
      this.loading = false;
    }, err => {
      this.error = 'Failed to run report';
      this.loading = false;
    });
  }
}
