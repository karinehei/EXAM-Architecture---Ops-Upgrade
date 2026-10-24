import { firstValueFrom } from 'rxjs';
import { DemoReportService } from './demo-report.service';
import {
  DEMO_STORAGE_KEY,
  DEMO_SEED_REPORTS,
  cloneSeedReports
} from './demo-data';

describe('DemoReportService', () => {
  let service: DemoReportService;

  beforeEach(() => {
    sessionStorage.removeItem(DEMO_STORAGE_KEY);
    service = DemoReportService.forTesting({ latencyMs: 0 });
  });

  afterEach(() => {
    sessionStorage.removeItem(DEMO_STORAGE_KEY);
  });

  it('lists seed reports with pagination metadata', async () => {
    const page = await firstValueFrom(service.list({ page: 1, pageSize: 5 }));
    expect(page.data.length).toBe(5);
    expect(page.pagination.total).toBe(DEMO_SEED_REPORTS.length);
    expect(page.pagination.totalPages).toBe(2);
    expect(page.data.every((r) => r.title.includes('[Demo]'))).toBeTrue();
  });

  it('filters by search and keeps pagination consistent', async () => {
    const page = await firstValueFrom(
      service.list({ filters: 'security', page: 1, pageSize: 5 })
    );
    expect(page.pagination.total).toBe(1);
    expect(page.data[0].title.toLowerCase()).toContain('security');
  });

  it('creates reports with unique ids', async () => {
    const a = await firstValueFrom(
      service.create({ title: 'Alpha', content: 'A content' })
    );
    const b = await firstValueFrom(
      service.create({ title: 'Beta', content: 'B content' })
    );
    expect(a.id).not.toBe(b.id);
    const all = await firstValueFrom(service.list({ pageSize: 100 }));
    expect(all.pagination.total).toBe(DEMO_SEED_REPORTS.length + 2);
  });

  it('updates and deletes while selection-friendly list stays coherent', async () => {
    const created = await firstValueFrom(
      service.create({ title: 'Temp', content: 'Temporary' })
    );
    const updated = await firstValueFrom(
      service.update(created.id, { title: 'Temp2', content: 'Updated body' })
    );
    expect(updated.title).toBe('Temp2');

    await firstValueFrom(service.delete(created.id));
    const after = await firstValueFrom(service.list({ pageSize: 100 }));
    expect(after.data.find((r) => r.id === created.id)).toBeUndefined();
    expect(after.pagination.total).toBe(DEMO_SEED_REPORTS.length);
  });

  it('validates required fields on create', async () => {
    try {
      await firstValueFrom(service.create({ title: '  ', content: 'x' }));
      fail('expected validation error');
    } catch (err: unknown) {
      expect((err as { status: number }).status).toBe(400);
    }
  });

  it('persists to sessionStorage and restores', async () => {
    await firstValueFrom(
      service.create({ title: 'Persisted', content: 'In session' })
    );
    const raw = sessionStorage.getItem(DEMO_STORAGE_KEY);
    expect(raw).toContain('Persisted');

    const again = DemoReportService.forTesting({ latencyMs: 0 });
    const page = await firstValueFrom(again.list({ pageSize: 100 }));
    expect(page.data.some((r) => r.title === 'Persisted')).toBeTrue();
  });

  it('recovers from corrupt sessionStorage using seed data', async () => {
    sessionStorage.setItem(DEMO_STORAGE_KEY, '{not-json');
    const recovered = DemoReportService.forTesting({ latencyMs: 0 });
    const page = await firstValueFrom(recovered.list({ pageSize: 100 }));
    expect(page.pagination.total).toBe(DEMO_SEED_REPORTS.length);
  });

  it('uses in-memory fallback when storage is forced off', async () => {
    sessionStorage.removeItem(DEMO_STORAGE_KEY);
    const mem = DemoReportService.forTesting({
      latencyMs: 0,
      forceMemory: true,
      initial: cloneSeedReports()
    });
    await firstValueFrom(
      mem.create({ title: 'Memory only', content: 'No storage write expected' })
    );
    // forceMemory skips persist — session must not contain the new title
    const raw = sessionStorage.getItem(DEMO_STORAGE_KEY);
    expect(raw === null || !raw.includes('Memory only')).toBeTrue();
    const page = await firstValueFrom(mem.list({ pageSize: 100 }));
    expect(page.data.some((r) => r.title === 'Memory only')).toBeTrue();
  });

  it('resetDemo restores seed and clears prior edits', async () => {
    await firstValueFrom(
      service.create({ title: 'Scratch', content: 'Will vanish' })
    );
    await firstValueFrom(service.resetDemo());
    const page = await firstValueFrom(service.list({ pageSize: 100 }));
    expect(page.pagination.total).toBe(DEMO_SEED_REPORTS.length);
    expect(page.data.some((r) => r.title === 'Scratch')).toBeFalse();
  });

  it('does not call fetch / HTTP during CRUD', async () => {
    const spy = spyOn(window, 'fetch');
    await firstValueFrom(service.list({ page: 1, pageSize: 5 }));
    await firstValueFrom(
      service.create({ title: 'No HTTP', content: 'Local only' })
    );
    expect(spy).not.toHaveBeenCalled();
  });
});
