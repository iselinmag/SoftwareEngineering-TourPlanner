import { Routes } from '@angular/router';
import { authGuard } from './services/auth.guard';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';

export const routes: Routes = [
  // open pages, anyone can reach these
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // the main app, only reachable once logged in
  { path: '', component: HomeComponent, canActivate: [authGuard] },

  // anything unknown sends the user back to the home route
  { path: '**', redirectTo: '' }
];
