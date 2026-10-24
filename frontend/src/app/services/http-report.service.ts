import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateReportRequest,
  ListReportsParams,
  PaginatedReports,
  Report,
  UpdateReportRequest
} from '../models/report.model';
import { ReportService } from './report.service';

@Injectable()
export class HttpReportService extends ReportService {
  private readonly baseUrl = `${environment.apiBaseUrl}/reports`;

  constructor(private readonly http: HttpClient) {
    super();
  }

  list(params: ListReportsParams = {}): Observable<PaginatedReports> {
    let httpParams = new HttpParams();
    if (params.filters) {
      httpParams = httpParams.set('filters', params.filters);
    }
    if (params.page != null) {
      httpParams = httpParams.set('page', String(params.page));
    }
    if (params.pageSize != null) {
      httpParams = httpParams.set('pageSize', String(params.pageSize));
    }
    return this.http.get<PaginatedReports>(this.baseUrl, { params: httpParams });
  }

  get(id: number): Observable<Report> {
    return this.http.get<Report>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateReportRequest): Observable<Report> {
    return this.http.post<Report>(this.baseUrl, request);
  }

  update(id: number, request: UpdateReportRequest): Observable<Report> {
    return this.http.put<Report>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  override resetDemo(): Observable<void> {
    return throwError(
      () => new Error('resetDemo is only available in demo mode')
    );
  }
}
