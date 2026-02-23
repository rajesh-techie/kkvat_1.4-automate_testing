import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProgressService {
  constructor(private http: HttpClient) {}

  getProgress(name: string): Observable<any> {
    return this.http.get(`/api/generator/progress/${name}`);
  }
}
