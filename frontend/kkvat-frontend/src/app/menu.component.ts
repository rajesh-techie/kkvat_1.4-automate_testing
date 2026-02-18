import { Component, Input } from '@angular/core';

import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule],
  template: `
    <ul class="menu-list">
      <ng-container *ngFor="let item of menus">
        <li *ngIf="!item.parentMenuItemId">
          <span>{{ item.displayName || item.name }}</span>
          <ul *ngIf="hasChildren(item)">
            <li *ngFor="let child of getChildren(item)">
              <span>{{ child.displayName || child.name }}</span>
            </li>
          </ul>
        </li>
      </ng-container>
    </ul>
  `,
  styles: [`.menu-list{list-style:none;padding:0;}li{margin:8px 0;}`]
})
export class MenuComponent {
  @Input() menus: any[] = [];

  hasChildren(item: any): boolean {
    return this.menus.some(m => m.parentMenuItemId === item.id);
  }

  getChildren(item: any) {
    return this.menus.filter(m => m.parentMenuItemId === item.id);
  }
}
