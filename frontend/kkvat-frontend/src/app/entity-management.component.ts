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
  model: any = {
    // 111,112
    entityName: '',
    entityTableName: '',
    // 113,114
    entityColumnsCount: null,
    // 115 multi
    entityColumnNext: '',
    isColumnDropdown: false,
    isColumnCheckbox: false,
    isColumnRadio: false,
    isColumnBlob: false,
    columnType: '',
    columnLength: null,
    columnPrimary: false,
    columnIndex: false,
    columnPartOfSearch: false,
    isReferentialIntegrity: false,
    // 116 (removed entity_column_end)
    // 117-119
    doWeNeedWorkflow: false,
    doWeNeed2LevelWorkflow: false,
    doWeNeed1LevelWorkflow: false,
    workflowStatus: '',
    // 120-121
    doWeNeedAuditTable: false,
    doWeNeedArchiveRecords: false,
    // 122a JSON
    criteriaFields: '',
    criteriaValues: '',
    // 122
    doWeNeedCreateView: false,
    // 123-124
    howManyMonthsMainTable: null,
    howManyMonthsArchiveTable: null,
    // 125-127 text areas
    criteriaToMoveFromMainToArchiveTable: '',
    criteriaToMoveFromArchiveToDeleteTable: '',
    thingsToCreate: '',
    // 128-129
    parentMenu: '',
    whichRoleIsEligible: '',
    // columns array to hold per-column definitions for the entity
    columns: []
  };
  apiBase = 'http://localhost:8080/api';
  generated: any[] = [];

  constructor(private svc: EntityManagementService) {}

  // Return columns as a normalized array; handles cases where backend returns object or JSON string
  normalizeColumns(inputCols: any): any[] {
    let cols = inputCols;
    if (!cols) return [];
    if (typeof cols === 'string') {
      try { cols = JSON.parse(cols); } catch (e) { return []; }
    }
    if (!Array.isArray(cols) && typeof cols === 'object') {
      cols = Object.values(cols);
    }

    cols = (cols || []).map((raw: any, idx: number) => {
      const c = raw || {};
      // support alternate key names that may come from older payloads
      const name = c.column_name ?? c.columnName ?? c.name ?? '';
      const seq = c.column_seq ?? c.seq ?? (idx + 1);
      const length = c.column_length ?? c.length ?? null;
      const datatype = c.column_datatype ?? c.datatype ?? 'string';
      const type = c.column_type ?? c.type ?? 'freefield';

      const col = {
        ...c,
        column_seq: Number(seq) || (idx + 1),
        column_name: name,
        column_length: length == null ? null : length,
        column_datatype: datatype,
        column_type: type,
        column_index: (c.column_index === 1 || c.column_index === true || String(c.column_index) === '1') ? 1 : 0,
        column_primary: (c.column_primary === 1 || c.column_primary === true || String(c.column_primary) === '1') ? 1 : 0,
        column_part_of_search: (c.column_part_of_search === 1 || c.column_part_of_search === true || String(c.column_part_of_search) === '1') ? 1 : 0,
        column_referential_integrity: (c.column_referential_integrity === 1 || c.column_referential_integrity === true || String(c.column_referential_integrity) === '1') ? 1 : 0
      };
      return col;
    });

    // filter out completely empty placeholder rows (no name and default flags)
    cols = cols.filter((c: any) => {
      const emptyName = !c.column_name || String(c.column_name).trim() === '';
      const allDefaults = c.column_datatype === 'string' && c.column_type === 'freefield' && c.column_length == null && c.column_index === 0 && c.column_primary === 0 && c.column_part_of_search === 0 && c.column_referential_integrity === 0;
      return !(emptyName && allDefaults);
    });

    return cols;
  }

  columnsList(): any[] {
    const cols = this.normalizeColumns(this.model?.columns);
    this.model.columns = cols;
    return cols;
  }

  // Ensure model.columns is a proper array (defensive against stringified JSON or object maps)
  ensureColumnsArray(): void {
    if (!this.model) this.model = { columns: [] } as any;
    let cols = this.model.columns;
    if (!cols) { this.model.columns = []; return; }
    if (typeof cols === 'string') {
      try {
        cols = JSON.parse(cols);
      } catch (e) {
        // fallback: try normalization (handles odd string formats)
        cols = this.normalizeColumns(cols as any);
      }
    }
    if (!Array.isArray(cols) && typeof cols === 'object') {
      cols = Object.values(cols as any);
    }
    // persist normalized array and re-run normalization for each item
    this.model.columns = this.normalizeColumns(cols);
  }

  ngOnInit(): void {
    console.log('EntityManagementComponent ngOnInit model.columns', this.model?.columns);
    console.log('EntityManagementComponent ngOnInit configs', this.configs);
    console.log('EntityManagementComponent ngOnInit generated', this.generated);
    this.load();
    this.loadGenerated();
  }

  load() {
    this.svc.list().subscribe(res => this.configs = res || []);
  }

  edit(item: any) {
    this.selected = item;
    // copy basic model; ensure columns array exists
    this.model = { ...item };
    // normalize incoming columns into an array for template binding
    this.model.columns = this.normalizeColumns(item?.columns);
    console.log('EntityManagementComponent edit model.columns', this.model.columns);
  }

  create() {
    // defensive: ensure columns is an Array and normalized before sending
    this.ensureColumnsArray();
    // ensure column_seq values exist and are in order before sending
    if (this.model.columns && Array.isArray(this.model.columns)) {
      for (let i = 0; i < this.model.columns.length; i++) {
        if (!this.model.columns[i].column_seq) this.model.columns[i].column_seq = i + 1;
        else this.model.columns[i].column_seq = Number(this.model.columns[i].column_seq) || (i + 1);
      }
    }

    // Create configuration then trigger generation
    this.svc.create(this.model).subscribe((created: any) => {
      this.load();
      if (created && created.id) {
        this.svc.generate(created.id).subscribe((res: any) => {
          alert('Generation finished: ' + (res?.message || 'OK'));
        }, (err: any) => {
          alert('Generation failed: ' + (err?.error?.message || err?.message || err));
        });
      }
      this.model = { entityName: '', entityTableName: '', columns: [] };
    });
  }

  addColumn() {
    if (!this.model.columns) this.model.columns = [];
    const nextSeq = this.model.columns.length + 1;
    this.model.columns.push({
      column_seq: nextSeq,
      column_name: '',
      column_length: null,
      column_datatype: 'string',
      column_type: 'freefield',
      is_dropdown: 0,
      is_radiobutton: 0,
      is_checkbox: 0,
      is_freefield: 1,
      column_index: 0,
      column_primary: 0,
      column_part_of_search: 0,
      column_referential_integrity: 0
    });
  }

  deleteColumn(idx: number) {
    if (!this.model.columns || idx < 0 || idx >= this.model.columns.length) return;
    this.model.columns.splice(idx, 1);
    // re-sequence column_seq values
    for (let i = 0; i < this.model.columns.length; i++) {
      this.model.columns[i].column_seq = i + 1;
    }
  }

  // Safe getter for template iteration to avoid NG0900 when columns is not an array
  get columnsForTemplate(): any[] {
    const cols = this.model?.columns;
    if (!cols) return [];
    if (Array.isArray(cols)) return cols;
    // fallback to normalization (handles stringified JSON or object maps)
    try {
      const normalized = this.normalizeColumns(cols);
      // persist normalized array back to model so subsequent bindings see array
      this.model.columns = normalized;
      return normalized;
    } catch (e) {
      return [];
    }
  }

  generate(id: number) {
    if (!confirm('Generate entity now?')) return;
    this.svc.generate(id).subscribe((res: any) => {
      alert('Generation finished: ' + (res?.message || 'OK'));
      this.load();
      this.loadGenerated();
    }, (err: any) => {
      alert('Generation failed: ' + (err?.error?.message || err?.message || err));
    });
  }

  loadGenerated() {
    this.svc.listGenerated().subscribe(res => { this.generated = res || []; console.log('loadGenerated result', this.generated); });
  }

  // Safe getter for generated artifacts iteration
  get generatedForTemplate(): any[] {
    const g = this.generated;
    if (!g) return [];
    if (Array.isArray(g)) return g;
    try {
      const arr = Array.isArray(g) ? g : Object.values(g);
      this.generated = arr;
      return arr;
    } catch (e) {
      return [];
    }
  }

  // Safe getter for configs iteration
  get configsForTemplate(): any[] {
    const c = this.configs;
    if (!c) return [];
    if (Array.isArray(c)) return c;
    try {
      const arr = Array.isArray(c) ? c : Object.values(c);
      this.configs = arr;
      return arr;
    } catch (e) {
      return [];
    }
  }

  deleteGenerated(name: string) {
    if (!confirm('Delete generated files for ' + name + ' ? This cannot be undone.')) return;
    this.svc.deleteGenerated(name).subscribe(() => { alert('Deleted: ' + name); this.loadGenerated(); }, (err:any)=>{ alert('Delete failed: '+(err?.error?.message||err?.message||err)); });
  }

  update() {
    if (!this.selected) return;
    // defensive: ensure columns is an Array and normalized before update
    this.ensureColumnsArray();
    if (this.model.columns && Array.isArray(this.model.columns)) {
      for (let i = 0; i < this.model.columns.length; i++) {
        this.model.columns[i].column_seq = this.model.columns[i].column_seq ? Number(this.model.columns[i].column_seq) : (i + 1);
      }
    }
    this.svc.update(this.selected.id, this.model).subscribe(() => { this.selected = null; this.model = { entityName: '', entityTableName: '', columns: [] }; this.load(); });
  }

  delete(id: number) {
    if (!confirm('Delete this configuration?')) return;
    this.svc.delete(id).subscribe(() => this.load());
  }
}
