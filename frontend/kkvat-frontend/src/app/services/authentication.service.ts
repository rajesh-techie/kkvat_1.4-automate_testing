import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private apiUrl = 'http://localhost:8080/api/auth/login';
  private tokenKey = 'accessToken';
  private menuKey = 'menus';
  private userKey = 'user';
  private menuSubject = new BehaviorSubject<any[]>([]);

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(this.apiUrl, { username, password }).pipe(
      tap(res => {
        if (res && res.accessToken) {
          localStorage.setItem(this.tokenKey, res.accessToken);
          localStorage.setItem(this.menuKey, JSON.stringify(res.menus || []));
          localStorage.setItem(this.userKey, JSON.stringify(res.user || {}));
          this.menuSubject.next(res.menus || []);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.menuKey);
    localStorage.removeItem(this.userKey);
    this.menuSubject.next([]);
  }

  getMenus(): any[] {
    return this.menuSubject.value.length ? this.menuSubject.value : JSON.parse(localStorage.getItem(this.menuKey) || '[]');
  }

  getMenusObservable() {
    return this.menuSubject.asObservable();
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }
}
