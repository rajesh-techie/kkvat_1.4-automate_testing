import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Tbl_users5Service } from './tbl_users5.service';
@Component({
  selector: 'app-tbl_users5',
  templateUrl: './tbl_users5.component.html',
  styleUrls: ['./tbl_users5.component.css']
})
export class Tbl_users5Component implements OnInit {
  rows: any[] = [];
  form: any = {};
  editingId: any = null;
  constructor(private svc: Tbl_users5Service) {}
  ngOnInit() { this.load(); }
  load() { this.svc.getAll().subscribe(r => this.rows = r); }
  onSubmit() {
    if (this.editingId) { this.svc.update(this.editingId, this.form).subscribe(() => { this.load(); this.form = {}; this.editingId = null; }); }
    else { this.svc.create(this.form).subscribe(() => { this.load(); this.form = {}; }); }
  }
  edit(row: any) { this.form = { ...row }; this.editingId = row.id; }
  delete(row: any) { if (confirm('Delete?')) this.svc.delete(row.id).subscribe(() => this.load()); }
}
