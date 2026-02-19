import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from './services/authentication.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  constructor(private auth: AuthenticationService, private router: Router) {}

  onSubmit() {
    if (!this.username || !this.password) {
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/landing']);
      },
      error: err => {
        this.isLoading = false;
        if (err?.status === 401) {
          this.errorMessage = 'Invalid username or password';
        } else if (err?.status === 0) {
          this.errorMessage = 'Cannot connect to server. Make sure backend is running on http://localhost:8080';
        } else {
          this.errorMessage = err?.error?.message || 'Login failed';
        }
      }
    });
  }
}
