import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

// this handles logging in, signing up and logging out on the frontend.
// when login works the backend hands back a ticket (token), and we tuck it away in the
// browser's local storage (a small box the browser keeps even after a refresh) so the user
// stays logged in. it also keeps track of who is logged in so screens can show the right buttons.
@Injectable({ providedIn: 'root' })
export class AuthService {

  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = 'http://localhost:8080/api/auth';

  // is someone logged in right now?
  isLoggedIn = signal<boolean>(!!localStorage.getItem('token'));

  // the username of whoever is logged in, used to decide which buttons to show
  currentUsername = signal<string | null>(localStorage.getItem('username'));

  // send the username and password to the backend, and if they are right, save the ticket
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

  // make a new account, then log the person straight in by saving the ticket we get back
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

  // throw away the ticket and the saved name, then send the user back to the login page
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.isLoggedIn.set(false);
    this.currentUsername.set(null);
    this.router.navigate(['/login']);
  }

  // hand back the saved ticket, or nothing if the user is not logged in
  getToken(): string | null {
    return localStorage.getItem('token');
  }
}