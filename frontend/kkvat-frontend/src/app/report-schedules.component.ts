import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-report-schedules',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="entity-page">
      <h2>Report Schedules</h2>
      <form (ngSubmit)="create()">
        <input [(ngModel)]="model.name" name="name" placeholder="Name" required />
        <textarea [(ngModel)]="model.cronExpression" name="cronExpression" placeholder="Cron expression"></textarea>
        <input [(ngModel)]="model.reportId" name="reportId" placeholder="Report ID" />
        <button type="submit">Create</button>
      </form>

      <ul>
        <li *ngFor="let s of schedules">{{s.name}} - {{s.cronExpression}} <button (click)="delete(s.id)">Delete</button></li>
      </ul>
    </div>
  `
})
export class ReportSchedulesComponent implements OnInit {
  schedules: any[] = [];
  model: any = { name: '', cronExpression: '', reportId: null };
  apiBase = 'http://localhost:8080/api/report-schedules';

  constructor(private http: HttpClient) {}

  ngOnInit() { this.load(); }

  load() { this.http.get<any>(this.apiBase).subscribe(res => { this.schedules = (res && res.content) || (res && res.data) || res || []; }); }

  create() { this.http.post<any>(this.apiBase, this.model).subscribe(() => { this.model = { name: '', cronExpression: '', reportId: null }; this.load(); }); }

  delete(id: number) { this.http.delete(`${this.apiBase}/${id}`).subscribe(() => this.load()); }
}
