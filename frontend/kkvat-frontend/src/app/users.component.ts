import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  model: any = { username: '', email: '', password: '', firstName: '', lastName: '', role: '', isActive: true };
  editingId: number | null = null;
  searchQuery = '';
  apiBase = 'http://localhost:8080/api/users';
  // pagination
  page = 0;
  size = 20;
  totalPages = 0;
  totalElements = 0;
  isSearching = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.load(0);
  }

  load(page: number = 0) {
    this.isSearching = false;
    this.page = page;
    this.http.get<any>(`${this.apiBase}?page=${page}&size=${this.size}`).subscribe(res => {
      this.users = (res && res.content) || (res && res.data) || res || [];
      this.totalPages = (res && res.totalPages) || 0;
      this.totalElements = (res && res.totalElements) || 0;
    });
  }

  search() {
    const q = (this.searchQuery || '').trim();
    if (!q) {
      this.load();
      return;
    }
    this.isSearching = true;
    this.http.get<any>(`${this.apiBase}/search?keyword=${encodeURIComponent(q)}`).subscribe(res => {
      this.users = res || [];
      this.totalPages = 0;
      this.totalElements = this.users.length;
    });
  }

  clearSearch() {
    this.searchQuery = '';
    this.load(0);
  }

  save() {
    if (this.editingId) {
      this.http.put<any>(`${this.apiBase}/${this.editingId}`, this.model).subscribe(() => {
        this.cancelEdit();
        this.load(this.page);
      });
    } else {
      this.http.post<any>(this.apiBase, this.model).subscribe(() => {
        this.model = { username: '', email: '', password: '', firstName: '', lastName: '', role: '', isActive: true };
        this.load(this.page);
      });
    }
  }

  startEdit(u: any) {
    const id = u.id || u.ID;
    this.editingId = id;
    this.model = { ...u };
  }

  cancelEdit() {
    this.editingId = null;
    this.model = { username: '', email: '', password: '', firstName: '', lastName: '', role: '', isActive: true };
  }

  delete(id: number) {
    this.http.delete(`${this.apiBase}/${id}`).subscribe(() => this.load(this.page));
  }
}
