import { Injectable } from '@angular/core';
import { Observable, of, throwError, delay } from 'rxjs';
import {
  CreateReportRequest,
  ListReportsParams,
  PaginatedReports,
  PaginationInfo,
  Report,
  UpdateReportRequest
} from '../models/report.model';
import { ReportService } from './report.service';
import {
  DEMO_STORAGE_KEY,
  cloneSeedReports,
  parseStoredReports
} from './demo-data';

/**
 * In-browser report store for the GitHub Pages demo.
 * Does not call backend, localhost, auth providers, or external APIs.
 */
@Injectable()
export class DemoReportService extends ReportService {
  private reports: Report[] = [];
  private nextId = 1;
  private useMemoryOnly: boolean;
  /** Artificial delay so loading states are visible (skipped in tests via 0). */
  private latencyMs: number;

  constructor() {
    super();
    this.latencyMs = 120;
    this.useMemoryOnly = !this.storageAvailable();
    this.reports = this.loadInitial();
    this.nextId = this.computeNextId(this.reports);
  }

  /** Test helper: construct with zero latency and optional initial state. */
  static forTesting(options?: {
    latencyMs?: number;
    forceMemory?: boolean;
    initial?: Report[];
  }): DemoReportService {
    const svc = new DemoReportService();
    svc.latencyMs = options?.latencyMs ?? 0;
    if (options?.forceMemory === true) {
      svc.clearDemoStorage();
      svc.useMemoryOnly = true;
    } else {
      svc.useMemoryOnly = !svc.storageAvailable();
    }
    svc.reports = options?.initial
      ? options.initial.map((r) => ({ ...r }))
      : svc.useMemoryOnly
        ? cloneSeedReports()
        : svc.loadInitial();
    svc.nextId = svc.computeNextId(svc.reports);
    return svc;
  }

  list(params: ListReportsParams = {}): Observable<PaginatedReports> {
    const page = Math.max(1, params.page ?? 1);
    const pageSize = Math.min(100, Math.max(1, params.pageSize ?? 20));
    const filter = (params.filters ?? '').trim().toLowerCase();

    let filtered = [...this.reports];
    if (filter) {
      filtered = filtered.filter(
        (r) =>
          r.title.toLowerCase().includes(filter) ||
          r.content.toLowerCase().includes(filter)
      );
    }

    filtered.sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    const total = filtered.length;
    const totalPages = total > 0 ? Math.ceil(total / pageSize) : 0;
    const start = (page - 1) * pageSize;
    const data = filtered.slice(start, start + pageSize).map((r) => ({ ...r }));

    const pagination: PaginationInfo = { page, pageSize, total, totalPages };
    return of({ data, pagination }).pipe(delay(this.latencyMs));
  }

  get(id: number): Observable<Report> {
    const found = this.reports.find((r) => r.id === id);
    if (!found) {
      return throwError(() => ({
        status: 404,
        message: `Report ${id} not found`
      }));
    }
    return of({ ...found }).pipe(delay(this.latencyMs));
  }

  create(request: CreateReportRequest): Observable<Report> {
    const title = request.title?.trim() ?? '';
    const content = request.content?.trim() ?? '';
    if (!title || !content) {
      return throwError(() => ({
        status: 400,
        message: 'Title and content are required'
      }));
    }
    const created: Report = {
      id: this.nextId++,
      title,
      content,
      createdAt: new Date().toISOString()
    };
    this.reports = [...this.reports, created];
    this.persist();
    return of({ ...created }).pipe(delay(this.latencyMs));
  }

  update(id: number, request: UpdateReportRequest): Observable<Report> {
    const title = request.title?.trim() ?? '';
    const content = request.content?.trim() ?? '';
    if (!title || !content) {
      return throwError(() => ({
        status: 400,
        message: 'Title and content are required'
      }));
    }
    const index = this.reports.findIndex((r) => r.id === id);
    if (index < 0) {
      return throwError(() => ({ status: 404, message: `Report ${id} not found` }));
    }
    const updated: Report = {
      ...this.reports[index],
      title,
      content
    };
    this.reports = [
      ...this.reports.slice(0, index),
      updated,
      ...this.reports.slice(index + 1)
    ];
    this.persist();
    return of({ ...updated }).pipe(delay(this.latencyMs));
  }

  delete(id: number): Observable<void> {
    const index = this.reports.findIndex((r) => r.id === id);
    if (index < 0) {
      return throwError(() => ({ status: 404, message: `Report ${id} not found` }));
    }
    this.reports = [
      ...this.reports.slice(0, index),
      ...this.reports.slice(index + 1)
    ];
    this.persist();
    return of(undefined).pipe(delay(this.latencyMs));
  }

  override resetDemo(): Observable<void> {
    this.clearDemoStorage();
    this.reports = cloneSeedReports();
    this.nextId = this.computeNextId(this.reports);
    this.persist();
    return of(undefined).pipe(delay(this.latencyMs));
  }

  /** Clears only this demo’s session key — not unrelated browser storage. */
  clearDemoStorage(): void {
    if (this.useMemoryOnly) {
      return;
    }
    try {
      sessionStorage.removeItem(DEMO_STORAGE_KEY);
    } catch {
      // ignore
    }
  }

  private loadInitial(): Report[] {
    if (this.useMemoryOnly) {
      return cloneSeedReports();
    }
    try {
      const stored = parseStoredReports(sessionStorage.getItem(DEMO_STORAGE_KEY));
      if (stored) {
        return stored;
      }
    } catch {
      // corrupt or unavailable — fall through to seed
    }
    const seed = cloneSeedReports();
    this.writeStorage(seed);
    return seed;
  }

  private persist(): void {
    if (this.useMemoryOnly) {
      return;
    }
    this.writeStorage(this.reports);
  }

  private writeStorage(reports: Report[]): void {
    try {
      sessionStorage.setItem(DEMO_STORAGE_KEY, JSON.stringify(reports));
    } catch {
      // Quota or unavailable — keep in-memory state
    }
  }

  private storageAvailable(): boolean {
    try {
      const probe = '__exam_demo_probe__';
      sessionStorage.setItem(probe, '1');
      sessionStorage.removeItem(probe);
      return true;
    } catch {
      return false;
    }
  }

  private computeNextId(reports: Report[]): number {
    if (reports.length === 0) {
      return 1;
    }
    return Math.max(...reports.map((r) => r.id)) + 1;
  }
}
