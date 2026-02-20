import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles.component.html',
  styleUrls: ['./roles.component.css']
})
export class RolesComponent implements OnInit {
  roles: any[] = [];
  rolesMaster: any[] = [];
  model: any = { name: '', description: '', isActive: true };
  editingId: number | null = null;
  searchQuery = '';
  apiBase = 'http://localhost:8080/api/roles';
  // pagination (not used for roles; backend returns all)
  page = 0;
  size = 20;
  totalPages = 0;
  totalElements = 0;
  isSearching = false;

  constructor(private http: HttpClient) {}

  ngOnInit() { this.loadAll(); }

  // Roles endpoint returns all roles in `data` - fetch once and filter client-side
  loadAll() {
    this.isSearching = false;
    this.http.get<any>(`${this.apiBase}`).subscribe(res => {
      console.log('roles loadAll response:', res);
      const arr = this.normalizeListResponse(res);
      this.rolesMaster = Array.isArray(arr) ? arr : [];
      this.roles = [...this.rolesMaster];
      this.totalElements = this.roles.length;
      this.totalPages = 0;
      console.log('parsed roles count:', this.rolesMaster.length);
    });
  }

  // Normalize various backend list response shapes into an array
  normalizeListResponse(res: any): any[] {
    if (!res) return [];
    // Common shapes: { data: [...] } or { status,message,data:[...] } or { content: [...], totalPages... }
    if (Array.isArray(res)) return res;
    if (res.data) {
      // data may itself be an object that contains the list in a field
      if (Array.isArray(res.data)) return res.data;
      // if data is an object like { roles: [...] } or { content: [...] }
      const inner = res.data;
      if (Array.isArray(inner.roles)) return inner.roles;
      if (Array.isArray(inner.content)) return inner.content;
      // fallback: try to find the first array-valued property
      for (const k of Object.keys(inner)) {
        if (Array.isArray(inner[k])) return inner[k];
      }
    }
    if (res.content && Array.isArray(res.content)) return res.content;
    // fallback: look for any top-level array property
    for (const k of Object.keys(res)) {
      if (Array.isArray(res[k])) return res[k];
    }
    return [];
  }

  save() {
    if (this.editingId) {
      this.http.put<any>(`${this.apiBase}/${this.editingId}`, this.model).subscribe(() => {
        this.cancelEdit();
        this.loadAll();
      });
    } else {
      this.http.post<any>(this.apiBase, this.model).subscribe(() => {
        this.model = { name: '', description: '', isActive: true };
        this.loadAll();
      });
    }
  }

  startEdit(r: any) {
    const id = r.id || r.ID;
    this.editingId = id;
    // shallow copy to avoid two-way binding mutating list entry until saved
    this.model = { ...r };
  }

  cancelEdit() {
    this.editingId = null;
    this.model = { name: '', description: '', isActive: true };
  }

  delete(id: number) { this.http.delete(`${this.apiBase}/${id}`).subscribe(() => this.loadAll()); }

  search() {
    const q = (this.searchQuery || '').trim().toLowerCase();
    if (!q) { this.roles = [...this.rolesMaster]; this.isSearching = false; this.totalElements = this.roles.length; return; }
    this.isSearching = true;
    this.roles = this.rolesMaster.filter(r => {
      const name = (r.name || '').toString().toLowerCase();
      const desc = (r.description || '').toString().toLowerCase();
      return name.includes(q) || desc.includes(q);
    });
    this.totalElements = this.roles.length;
  }

  clearSearch() { this.searchQuery = ''; this.roles = [...this.rolesMaster]; this.isSearching = false; this.totalElements = this.roles.length; }
}
