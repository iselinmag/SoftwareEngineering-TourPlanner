import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
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
        next: (res) => this.storeSession(res.token, username),
        // tell the user what actually went wrong, not just one message for everything
        error: (err: HttpErrorResponse) => alert(this.explain(err, 'login'))
      });
  }

  // make a new account, then log the person straight in by saving the ticket we get back
  register(username: string, password: string) {
    return this.http.post<{ token: string }>(`${this.apiUrl}/register`, { username, password })
      .subscribe({
        next: (res) => this.storeSession(res.token, username),
        error: (err: HttpErrorResponse) => alert(this.explain(err, 'register'))
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

  // keep the ticket and the name so they survive a page refresh, then go to the start page
  private storeSession(token: string, username: string) {
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
    this.isLoggedIn.set(true);
    this.currentUsername.set(username);
    this.router.navigate(['/']);
  }

  // turn a raw http error into a message a person can actually act on.
  // status 0 means the request never reached the backend at all (server down, wrong port),
  // which is a completely different problem than typing the wrong password.
  private explain(err: HttpErrorResponse, action: 'login' | 'register'): string {
    if (err.status === 0) {
      return 'Cannot reach the server. Is the backend running on port 8080?';
    }
    if (err.status === 401) {
      return 'Wrong username or password.';
    }
    if (err.status === 409) {
      return 'That username is already taken.';
    }
    if (err.status === 400) {
      return 'Username and password must not be empty.';
    }
    // anything else is a problem on the server side, not the user's fault
    return `Something went wrong on the server (${err.status}). Check the backend logs.`;
  }
}