import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthenticationService } from './services/authentication.service';
import { MenuComponent } from './menu.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, MenuComponent, RouterModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {
  menus: any[] = [];
  userInfo: any = {};

  constructor(private auth: AuthenticationService, private router: Router) {}

  ngOnInit() {
    // initialize menus from service and subscribe to updates
    this.menus = this.auth.getMenus();
    this.auth.getMenusObservable().subscribe(m => this.menus = m || []);
    // Ensure Reports Generator is added as a child under the existing Reports menu
    const menusArr = this.menus || [];
    const hasChild = menusArr.some(mi => {
      const children = mi.childMenuItems ?? mi.children ?? [];
      return Array.isArray(children) && children.some((c: any) => (c.Name || c.name) === 'Reports Generator');
    });
    if (!hasChild) {
      // Try to find Reports parent menu by common fields
      const reportsParent = menusArr.find(mi => {
        const name = (mi.Name || mi.name || mi.displayName || '').toString().toLowerCase();
        const link = (mi.Link || mi.link || mi.path || '').toString().toLowerCase();
        return name === 'reports' || link.includes('/reports') || link === 'reports';
      });
      const child = { id: 'reports-generator', Name: 'Reports Generator', Link: 'landing/reports/generator' };
      if (reportsParent) {
        if (Array.isArray(reportsParent.childMenuItems)) {
          reportsParent.childMenuItems.push(child);
        } else if (Array.isArray(reportsParent.children)) {
          reportsParent.children.push(child);
        } else {
          // attach as nested children
          reportsParent.childMenuItems = [child];
        }
      } else {
        // fallback: append under root so it's accessible
        menusArr.push(child);
      }
      this.menus = menusArr;
      this.auth.setMenus(this.menus);
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}

