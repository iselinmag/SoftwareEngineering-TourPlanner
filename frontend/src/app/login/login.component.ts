import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

// this is the login screen.
// it holds the two boxes for username and password, and when the button is pressed it hands
// them to the auth service to do the real logging in. the screen itself stays simple.
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private auth = inject(AuthService);

  // these two hold whatever the user types into the boxes
  username = signal('');
  password = signal('');

  // called when the user clicks the login button
  submit() {
    this.auth.login(this.username(), this.password());
  }
}
