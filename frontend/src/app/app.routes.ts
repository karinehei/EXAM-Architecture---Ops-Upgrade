import { Routes } from '@angular/router';
import { ReportListComponent } from './report-list/report-list.component';
import { ArchitectureComponent } from './architecture/architecture.component';
import { OperationsComponent } from './operations/operations.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'reports' },
  { path: 'reports', component: ReportListComponent },
  { path: 'architecture', component: ArchitectureComponent },
  { path: 'operations', component: OperationsComponent },
  { path: '**', redirectTo: 'reports' }
];
