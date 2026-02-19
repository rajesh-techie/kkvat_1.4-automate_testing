
import { Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { LandingComponent } from './landing.component';
import { UsersComponent } from './users.component';
import { GroupsComponent } from './groups.component';
import { RolesComponent } from './roles.component';
import { TestcasesComponent } from './testcases.component';
import { ReportSchedulesComponent } from './report-schedules.component';
import { MenuItemsComponent } from './menu-items.component';
import { RoleMenuAssignComponent } from './role-menu-assign.component';
import { ReportsComponent } from './reports.component';
import { ReportsDownloadComponent } from './reports-download.component';
import { GroupUsersComponent } from './group-users.component';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent },
	{
		path: 'landing',
		component: LandingComponent,
		children: [
			{ path: '', redirectTo: 'welcome', pathMatch: 'full' },
			// Backwards-compat redirect for old/legacy menu path
			{ path: 'report-builder', redirectTo: 'reports', pathMatch: 'full' },
			{ path: 'users', component: UsersComponent },
			{ path: 'groups', component: GroupsComponent },
			{ path: 'groups/:id/users', component: GroupUsersComponent },
			{ path: 'roles', component: RolesComponent },
			{ path: 'testcases', component: TestcasesComponent },
			{ path: 'report-schedules', component: ReportSchedulesComponent },
			{ path: 'menu-items', component: MenuItemsComponent },
			{ path: 'role-menu-assign', component: RoleMenuAssignComponent },
			{ path: 'reports', component: ReportsComponent },
			{ path: 'report-downloads', component: ReportsDownloadComponent },
			{ path: 'welcome', loadComponent: () => import('./landing.component').then(m => m.LandingComponent) }
		]
	}
];
