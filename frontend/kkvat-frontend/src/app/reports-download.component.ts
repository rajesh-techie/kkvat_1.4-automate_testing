import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-reports-download',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="entity-page">
      <h2>Report Executions</h2>
      <ul>
        <li *ngFor="let e of executions">{{e.id}} - {{e.status}} <button (click)="download(e.id)">Download</button></li>
      </ul>
    </div>
  `
})
export class ReportsDownloadComponent implements OnInit {
  executions: any[] = [];
  constructor(private http: HttpClient) {}
  ngOnInit() { this.http.get<any>('http://localhost:8080/api/report-executions/my-executions').subscribe(res => this.executions = (res && res.content) || res || []); }
  download(id: number) {
    this.http.get(`http://localhost:8080/api/report-executions/download/${id}`, { responseType: 'blob' }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${id}.csv`;
      a.click();
      window.URL.revokeObjectURL(url);
    }, err => alert('Error downloading'));
  }
}
