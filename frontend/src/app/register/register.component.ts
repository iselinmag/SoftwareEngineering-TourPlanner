import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

// this is the sign up screen.
// same idea as the login screen: two boxes for a new username and password, and pressing the
// button hands them to the auth service to create the account.
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private auth = inject(AuthService);

  // these hold whatever the user types into the boxes
  username = signal('');
  password = signal('');

  // called when the user clicks the register button
  submit() {
    this.auth.register(this.username(), this.password());
  }
}
