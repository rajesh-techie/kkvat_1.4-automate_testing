import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { routes } from './app.routes';
import { AuthInterceptor } from './auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(HttpClientModule),
    provideRouter(routes),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ]
};
