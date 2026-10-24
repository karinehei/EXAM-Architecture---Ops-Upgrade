import {
  isValidReport,
  parseStoredReports,
  DEMO_SEED_REPORTS
} from './demo-data';

describe('demo-data helpers', () => {
  it('validates report shape', () => {
    expect(isValidReport(DEMO_SEED_REPORTS[0])).toBeTrue();
    expect(isValidReport({ id: 'x' })).toBeFalse();
    expect(isValidReport(null)).toBeFalse();
  });

  it('rejects corrupt stored payloads', () => {
    expect(parseStoredReports(null)).toBeNull();
    expect(parseStoredReports('[]')).toBeNull();
    expect(parseStoredReports('{"no":"array"}')).toBeNull();
    expect(parseStoredReports('[{ "id": 1 }]')).toBeNull();
  });

  it('accepts a valid stored array', () => {
    const raw = JSON.stringify(DEMO_SEED_REPORTS);
    const parsed = parseStoredReports(raw);
    expect(parsed?.length).toBe(DEMO_SEED_REPORTS.length);
  });
});
