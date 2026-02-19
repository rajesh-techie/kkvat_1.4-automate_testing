import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    try {
      const token = localStorage.getItem('accessToken');
      // Don't attach token for login requests
      if (token && !req.url.includes('/api/auth/login')) {
        const cloned = req.clone({
          setHeaders: { Authorization: `Bearer ${token}` }
        });
        return next.handle(cloned);
      }
    } catch (e) {
      // ignore
    }
    return next.handle(req);
  }
}
