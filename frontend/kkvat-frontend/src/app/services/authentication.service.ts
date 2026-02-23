import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, switchMap, catchError, map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private apiUrl = 'http://localhost:8080/api/auth/login';
  private tokenKey = 'accessToken';
  private menuKey = 'menus';
  private userKey = 'user';
  private sessionKey = 'sessionId';
  private menuSubject = new BehaviorSubject<any[]>([]);

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(this.apiUrl, { username, password }).pipe(
      switchMap(res => {
        if (res && res.accessToken) {
          localStorage.setItem(this.tokenKey, res.accessToken);
          localStorage.setItem(this.userKey, JSON.stringify(res.user || {}));
          if (res.sessionId) {
            localStorage.setItem(this.sessionKey, String(res.sessionId));
          }
          // Fetch menus separately from the menu API (hierarchical) to ensure correct shape
          const userId = res.user?.id;
          if (userId) {
            return this.http.get<any>(`http://localhost:8080/api/menu-items/user/${userId}/hierarchical`).pipe(
              tap(menuRes => {
                const menus = (menuRes && menuRes.data) ? menuRes.data : (Array.isArray(menuRes) ? menuRes : []);
                localStorage.setItem(this.menuKey, JSON.stringify(menus || []));
                this.menuSubject.next(menus || []);
              }),
              map(() => res),
              catchError(() => {
                // If fetching menus fails, continue but with empty menus
                localStorage.setItem(this.menuKey, JSON.stringify([]));
                this.menuSubject.next([]);
                return of(res);
              })
            );
          }
          // No userId, just set empty menus
          localStorage.setItem(this.menuKey, JSON.stringify([]));
          this.menuSubject.next([]);
        }
        return of(res);
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

  setMenus(menus: any[]) {
    try {
      localStorage.setItem(this.menuKey, JSON.stringify(menus || []));
    } catch (e) {}
    this.menuSubject.next(menus || []);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }
}
