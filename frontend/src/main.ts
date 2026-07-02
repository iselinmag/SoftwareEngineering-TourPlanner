import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// this is the on switch for the whole frontend.
// when the page loads, this starts up angular and puts the main app component on screen,
// using the settings from app.config. if anything goes wrong on startup it prints the error.
bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));