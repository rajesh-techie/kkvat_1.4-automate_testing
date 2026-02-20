import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EntityManagementService } from './entity-management.service';

@Component({
  selector: 'app-entity-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './entity-management.component.html',
  styleUrls: ['./entity-management.component.css']
})
export class EntityManagementComponent implements OnInit {
  configs: any[] = [];
  selected: any = null;
  model: any = { entityName: '', entityTableName: '' };
  apiBase = 'http://localhost:8080/api';

  constructor(private svc: EntityManagementService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.svc.list().subscribe(res => this.configs = res || []);
  }

  edit(item: any) {
    this.selected = item;
    this.model = { ...item };
  }

  create() {
    this.svc.create(this.model).subscribe(() => { this.model = { entityName: '', entityTableName: '' }; this.load(); });
  }

  update() {
    if (!this.selected) return;
    this.svc.update(this.selected.id, this.model).subscribe(() => { this.selected = null; this.model = { entityName: '', entityTableName: '' }; this.load(); });
  }

  delete(id: number) {
    if (!confirm('Delete this configuration?')) return;
    this.svc.delete(id).subscribe(() => this.load());
  }
}
