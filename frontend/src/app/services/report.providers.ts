import { Provider } from '@angular/core';
import { environment } from '../../environments/environment';
import { ReportService } from './report.service';
import { HttpReportService } from './http-report.service';
import { DemoReportService } from './demo-report.service';

/**
 * Selects the report implementation at build time.
 * Demo builds never use HTTP; normal builds never use synthetic data.
 */
export function provideReportService(): Provider[] {
  if (environment.demo) {
    return [
      DemoReportService,
      { provide: ReportService, useExisting: DemoReportService }
    ];
  }
  return [
    HttpReportService,
    { provide: ReportService, useExisting: HttpReportService }
  ];
}
