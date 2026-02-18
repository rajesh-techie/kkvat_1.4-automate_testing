import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from './services/authentication.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <h2>Login</h2>
      <form (ngSubmit)="onSubmit()">
        <input [(ngModel)]="username" name="username" placeholder="Username" required />
        <input [(ngModel)]="password" name="password" type="password" placeholder="Password" required />
        <button type="submit" [disabled]="isLoading">{{ isLoading ? 'Logging in...' : 'Login' }}</button>
      </form>
      <div *ngIf="errorMessage" class="error">{{ errorMessage }}</div>
    </div>
  `,
  styles: [`.login-container{max-width:320px;margin:60px auto;padding:24px;border-radius:8px;box-shadow:0 2px 8px #ccc;text-align:center;}input{display:block;width:100%;margin:12px 0;padding:8px;}button{width:100%;padding:8px;} .error{color:red;margin-top:10px;}`]
})
export class LoginComponent {
  username = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  constructor(private auth: AuthenticationService, private router: Router) {}

  onSubmit() {
    if (!this.username || !this.password) return;
    this.isLoading = true;
    this.errorMessage = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/landing']);
      },
      error: err => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || 'Login failed';
      }
    });
  }
}
