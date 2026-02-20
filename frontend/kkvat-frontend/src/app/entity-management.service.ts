import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class EntityManagementService {
  api = 'http://localhost:8080/api/entity-management';
  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<any[]>(this.api);
  }

  get(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  create(body: any) {
    return this.http.post<any>(this.api, body);
  }

  update(id: number, body: any) {
    return this.http.put<any>(`${this.api}/${id}`, body);
  }

  delete(id: number) {
    return this.http.delete(`${this.api}/${id}`);
  }
}
