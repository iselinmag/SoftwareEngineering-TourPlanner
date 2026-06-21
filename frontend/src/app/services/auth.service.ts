import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = 'http://localhost:8080/api/auth';

  // is someone logged in right now?
  isLoggedIn = signal<boolean>(!!localStorage.getItem('token'));

  // the username of whoever is logged in, used to decide which buttons to show
  currentUsername = signal<string | null>(localStorage.getItem('username'));

  login(username: string, password: string) {
    return this.http.post<{ token: string }>(`${this.apiUrl}/login`, { username, password })
      .subscribe({
        next: (res) => {
          // keep the ticket so it survives a page refresh
          localStorage.setItem('token', res.token);
          // remember who we are so the ui can show our own edit and delete buttons
          localStorage.setItem('username', username);
          this.isLoggedIn.set(true);
          this.currentUsername.set(username);
          this.router.navigate(['/']);
        },
        error: () => alert('Wrong username or password')
      });
  }

  register(username: string, password: string) {
    return this.http.post<{ token: string }>(`${this.apiUrl}/register`, { username, password })
      .subscribe({
        next: (res) => {
          localStorage.setItem('token', res.token);
          localStorage.setItem('username', username);
          this.isLoggedIn.set(true);
          this.currentUsername.set(username);
          this.router.navigate(['/']);
        },
        error: () => alert('Could not register, username may be taken')
      });
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.isLoggedIn.set(false);
    this.currentUsername.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}