
import { Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { LandingComponent } from './landing.component';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent },
	{ path: 'landing', component: LandingComponent },
];
