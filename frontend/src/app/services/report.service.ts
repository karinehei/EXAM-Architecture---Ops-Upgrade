import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateReportRequest,
  ListReportsParams,
  PaginatedReports,
  Report,
  UpdateReportRequest
} from '../models/report.model';

/**
 * Injectable abstraction for report CRUD.
 * Normal builds → HttpReportService; demo builds → DemoReportService.
 * Never fall back from HTTP failures to demo data.
 */
export abstract class ReportService {
  abstract list(params?: ListReportsParams): Observable<PaginatedReports>;
  abstract get(id: number): Observable<Report>;
  abstract create(request: CreateReportRequest): Observable<Report>;
  abstract update(id: number, request: UpdateReportRequest): Observable<Report>;
  abstract delete(id: number): Observable<void>;
  /** Demo-only; HTTP adapter throws if called. */
  abstract resetDemo?(): Observable<void>;
}

export const REPORT_SERVICE = new InjectionToken<ReportService>('ReportService');
