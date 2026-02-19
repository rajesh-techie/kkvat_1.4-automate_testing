import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent {
  // accept either structured old-style menuItems or new-style menus
  @Input() menuItems: any[] = [];
  @Input() menus: any[] = [];
  expandedMenus: Set<string> = new Set();

  private src(): any[] {
    return (this.menuItems && this.menuItems.length) ? this.menuItems : this.menus || [];
  }

  getId(item: any): string {
    if (item == null) return '';
    return String(item.ID ?? item.id ?? item.menuId ?? '');
  }

  getName(item: any): string {
    return item?.Name ?? item?.displayName ?? item?.name ?? '';
  }

  getLink(item: any): string {
    return item?.Link ?? item?.routeLink ?? item?.link ?? item?.path ?? '';
  }

  getParent(item: any): string | null {
    let p = item?.Parent ?? item?.parentMenuItemId ?? item?.parentId ?? item?.parent ?? null;
    // Normalize falsy/zero parent to null (database may use 0)
    if (p === '' || p === 0 || p === '0' || p === undefined) return null;
    if (p == null) return null;
    return String(p);
  }

  toggleMenu(menuId: string) {
    const id = String(menuId || '');
    if (!id) return;
    if (this.expandedMenus.has(id)) this.expandedMenus.delete(id); else this.expandedMenus.add(id);
  }

  isExpanded(menuId: string): boolean { return this.expandedMenus.has(menuId); }

  getChildMenus(parentId: string) {
    const srcArr = this.src();
    // Try hierarchical payload where children are nested under parent (childMenuItems)
    const parent = srcArr.find((item: any) => this.getId(item) === String(parentId));
    if (parent) {
      const children = parent.childMenuItems ?? parent.children ?? [];
      // Normalize returned children to array of items
      return Array.isArray(children) ? children : [];
    }
    // Fallback: flat list where each child has a parent id field
    return srcArr.filter((item: any) => this.getParent(item) === parentId);
  }

  getRootMenus() {
    return this.src().filter((item: any) => this.getParent(item) == null);
  }

  navigateTo(item: any) {
    if (!item) return;
    const l = this.getLink(item);
    // If link is provided and is a route path
    if (l && l !== '#') {
      const childPath = l.startsWith('/') ? l.substring(1) : l;
      // If link already includes landing prefix, navigate by url
      if (childPath.startsWith('landing/')) {
        this.router.navigateByUrl('/' + childPath);
      } else {
        this.router.navigate(['/landing', childPath]);
      }
      return;
    }

    // Fallback: try routeLink/displayName -> kebab-case
    const route = (item.routeLink ?? item.route ?? item.path) || '';
    if (route) {
      const childPath = route.startsWith('/') ? route.substring(1) : route;
      this.router.navigate(['/landing', childPath]);
      return;
    }

    const name = this.getName(item);
    if (name) {
      const guessed = name.toLowerCase().replace(/\s+/g, '-');
      this.router.navigate(['/landing', guessed]);
    }
  }

  hasChildren(menuId: string) { return this.getChildMenus(menuId).length > 0; }

  getRoute(item: any): any {
    const l = this.getLink(item);
    if (l && l !== '#') {
      let childPath = l.startsWith('/') ? l.substring(1) : l;
      // If the path contains a parent prefix like admin/users, use the last segment
      if (childPath.indexOf('/') !== -1) {
        const parts = childPath.split('/').filter((p: string) => p.length);
        childPath = parts[parts.length - 1];
      }
      if (childPath.startsWith('landing/')) return '/' + childPath;
      return '/landing/' + childPath;
    }
    const route = (item.routeLink ?? item.route ?? item.path) || '';
    if (route) {
      let r = route.startsWith('/') ? route.substring(1) : route;
      if (r.indexOf('/') !== -1) { const parts = r.split('/').filter((p: string) => p.length); r = parts[parts.length-1]; }
      return '/landing/' + r;
    }
    const name = this.getName(item);
    if (name) return '/landing/' + name.toLowerCase().replace(/\s+/g, '-');
    return '/landing';
  }
  constructor(private router: Router) {}
}
