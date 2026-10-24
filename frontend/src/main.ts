import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, withHashLocation } from '@angular/router';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { provideReportService } from './app/services/report.providers';
import { environment } from './environments/environment';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(),
    provideRouter(routes, ...(environment.demo ? [withHashLocation()] : [])),
    ...provideReportService()
  ]
}).catch((err) => console.error(err));
