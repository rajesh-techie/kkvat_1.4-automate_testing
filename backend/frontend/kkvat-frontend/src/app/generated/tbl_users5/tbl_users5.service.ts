import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
@Injectable({ providedIn: 'root' })
export class Tbl_users5Service {
  base = '/api/generated/tbl_users5';
  constructor(private http: HttpClient) {}
  getAll() { return this.http.get<any[]>(this.base); }
  create(data: any) { return this.http.post(this.base, data); }
  update(id: any, data: any) { return this.http.put(this.base + '/' + id, data); }
  delete(id: any) { return this.http.delete(this.base + '/' + id); }
}
