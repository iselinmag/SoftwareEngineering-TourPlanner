import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// this is the outer shell of the app.
// it holds almost nothing itself, just a slot (router-outlet) where the current page is shown.
// think of it as an empty picture frame, and the routing decides which picture goes inside.
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {
  title = 'tour-planner';
}
