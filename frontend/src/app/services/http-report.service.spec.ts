import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { HttpReportService } from './http-report.service';
import { environment } from '../../environments/environment';
import { provideReportService } from './report.providers';
import { ReportService } from './report.service';
import { HttpReportService as HttpImpl } from './http-report.service';
import { DemoReportService } from './demo-report.service';

describe('HttpReportService', () => {
  let service: HttpReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HttpReportService]
    });
    service = TestBed.inject(HttpReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists reports from the real API path', () => {
    service.list({ page: 1, pageSize: 20, filters: 'demo' }).subscribe((res) => {
      expect(res.data.length).toBe(1);
    });

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/reports` &&
        r.params.get('page') === '1' &&
        r.params.get('filters') === 'demo'
    );
    expect(req.request.method).toBe('GET');
    req.flush({
      data: [
        {
          id: 1,
          title: 'Live',
          content: 'From API',
          createdAt: '2026-01-10T00:00:00.000Z'
        }
      ],
      pagination: { page: 1, pageSize: 20, total: 1, totalPages: 1 }
    });
  });

  it('create/update/delete hit API endpoints only', () => {
    service.create({ title: 'N', content: 'C' }).subscribe();
    const post = httpMock.expectOne(`${environment.apiBaseUrl}/reports`);
    expect(post.request.method).toBe('POST');
    post.flush({
      id: 9,
      title: 'N',
      content: 'C',
      createdAt: '2026-01-10T00:00:00.000Z'
    });

    service.update(9, { title: 'U', content: 'C2' }).subscribe();
    const put = httpMock.expectOne(`${environment.apiBaseUrl}/reports/9`);
    expect(put.request.method).toBe('PUT');
    put.flush({
      id: 9,
      title: 'U',
      content: 'C2',
      createdAt: '2026-01-10T00:00:00.000Z'
    });

    service.delete(9).subscribe();
    const del = httpMock.expectOne(`${environment.apiBaseUrl}/reports/9`);
    expect(del.request.method).toBe('DELETE');
    del.flush(null);
  });

  it('does not fall back to synthetic data on API failure', () => {
    let failed = false;
    service.list().subscribe({
      next: () => fail('should not succeed'),
      error: () => {
        failed = true;
      }
    });
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/reports`);
    req.flush(
      { error: 'INTERNAL_ERROR', message: 'Failed' },
      { status: 500, statusText: 'Server Error' }
    );
    expect(failed).toBeTrue();
  });

  it('rejects resetDemo', (done) => {
    service.resetDemo().subscribe({
      next: () => fail('should not reset'),
      error: (err: Error) => {
        expect(err.message).toContain('demo mode');
        done();
      }
    });
  });
});

describe('provideReportService isolation (non-demo environment)', () => {
  it('wires HttpReportService when environment.demo is false', () => {
    expect(environment.demo).toBeFalse();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: provideReportService()
    });
    const svc = TestBed.inject(ReportService);
    expect(svc instanceof HttpImpl).toBeTrue();
    expect(svc instanceof DemoReportService).toBeFalse();
  });
});
