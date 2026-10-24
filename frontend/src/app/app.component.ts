import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive]
})
export class AppComponent {
  readonly isDemo = environment.demo;
  readonly title = this.isDemo
    ? 'EXAM Architecture & Operations Showcase'
    : 'EXAM System';
}
