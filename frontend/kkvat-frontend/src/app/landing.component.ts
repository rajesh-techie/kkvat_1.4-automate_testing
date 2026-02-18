import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from './services/authentication.service';
import { MenuComponent } from './menu.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [MenuComponent],
  template: `
    <div class="landing-container">
      <h2>Welcome!</h2>
      <app-menu [menus]="menus"></app-menu>
      <button (click)="logout()">Logout</button>
    </div>
  `,
  styles: [`.landing-container{max-width:600px;margin:40px auto;padding:24px;text-align:center;}`]
})
export class LandingComponent implements OnInit {
  menus: any[] = [];

  constructor(private auth: AuthenticationService, private router: Router) {}

  ngOnInit() {
    this.menus = this.auth.getMenus();
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
