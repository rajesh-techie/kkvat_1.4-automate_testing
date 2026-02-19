import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-testcases',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './testcases.component.html',
  styleUrls: ['./testcases.component.css']
})
export class TestcasesComponent implements OnInit {
  testcases: any[] = [];
  model: any = { name: '', description: '', recordedActions: '', status: 'ACTIVE', groupId: null, tags: '', baseUrl: '', timeoutSeconds: 30 };
  apiBase = 'http://localhost:8080/api/test-cases';

  constructor(private http: HttpClient) {}

  ngOnInit() { this.load(); }

  load() { this.http.get<any>(`${this.apiBase}/list`).subscribe(res => this.testcases = res || []); }

  create() { this.http.post<any>(this.apiBase, this.model).subscribe(() => { this.model = { name: '', description: '', recordedActions: '', status: 'ACTIVE', groupId: null, tags: '', baseUrl: '', timeoutSeconds: 30 }; this.load(); }); }

  delete(id: number) { this.http.delete(`${this.apiBase}/${id}`).subscribe(() => this.load()); }
}
