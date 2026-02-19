import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-role-menu-assign',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="entity-page">
      <h2>Assign Menu Items to Role</h2>
      <div>
        <label>Role:</label>
        <select [(ngModel)]="selectedRoleId" name="role">
          <option *ngFor="let r of roles" [value]="r.id">{{r.name}}</option>
        </select>
      </div>

      <div *ngIf="menuItems.length">
        <label *ngFor="let m of menuItems"><input type="checkbox" [value]="m.id" (change)="toggleMenuItem(m.id, $event)" /> {{m.name}}</label>
      </div>

      <button (click)="save()">Save</button>
    </div>
  `
})
export class RoleMenuAssignComponent implements OnInit {
  roles: any[] = [];
  menuItems: any[] = [];
  selectedRoleId: number | null = null;
  selectedMenuIds = new Set<number>();

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<any>('http://localhost:8080/api/roles').subscribe(res => this.roles = (res && res.data) || res || []);
    this.http.get<any>('http://localhost:8080/api/menu-items').subscribe(res => this.menuItems = (res && res.data) || res || []);
  }

  toggleMenuItem(id: number, ev: any) {
    if (ev.target.checked) this.selectedMenuIds.add(id); else this.selectedMenuIds.delete(id);
  }

  save() {
    if (!this.selectedRoleId) return;
    // PUT role with menuItems array of objects {id}
    const payload: any = { id: this.selectedRoleId, menuItems: Array.from(this.selectedMenuIds).map(i => ({ id: i })) };
    this.http.put<any>(`http://localhost:8080/api/roles/${this.selectedRoleId}`, payload).subscribe(() => alert('Saved'), err => alert('Error: '+(err.message||err.status)));
  }
}
