import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-menu-items',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './menu-items.component.html',
  styleUrls: ['./menu-items.component.css']
})
export class MenuItemsComponent implements OnInit {
  items: any[] = [];
  model: any = { name: '', displayName: '', routeLink: '', iconName: '', parentMenuItemId: null, menuOrder: 0, isActive: true };
  apiBase = 'http://localhost:8080/api/menu-items';

  constructor(private http: HttpClient) {}

  ngOnInit() { this.load(); }

  load() { this.http.get<any>(this.apiBase).subscribe(res => this.items = (res && res.data) || res || []); }

  create() { this.http.post<any>(this.apiBase, this.model).subscribe(() => { this.model = { name: '', displayName: '', routeLink: '', iconName: '', parentMenuItemId: null, menuOrder: 0, isActive: true }; this.load(); }); }

  delete(id: number) { this.http.delete(`${this.apiBase}/${id}`).subscribe(() => this.load()); }
}
