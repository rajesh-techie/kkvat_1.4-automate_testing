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
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}

