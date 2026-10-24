import { Report } from '../models/report.model';

/**
 * Fictional portfolio sample data — not real student records.
 * Fixed creation dates keep the demo deterministic.
 */
export const DEMO_SEED_REPORTS: readonly Report[] = [
  {
    id: 1,
    title: '[Demo] Spring load-test summary',
    content:
      'Synthetic summary of a fictional k6 run against the reports list endpoint. ' +
      'No real exam or student data. Used to illustrate pagination and search in the portfolio demo.',
    createdAt: '2026-01-09T10:15:00.000Z'
  },
  {
    id: 2,
    title: '[Demo] Architecture notes — API boundary',
    content:
      'Fictional notes on how the Angular client calls Play/Scala REST endpoints at /api/v1/reports. ' +
      'Describes reverse-proxy placement and auth boundary at a high level for visitors.',
    createdAt: '2026-01-10T14:30:00.000Z'
  },
  {
    id: 3,
    title: '[Demo] Ops scenario checklist',
    content:
      'Illustrative checklist an operator might follow when API latency rises: check correlation IDs, ' +
      'database connectivity, and rate-limit windows. Values shown elsewhere in the demo are simulated.',
    createdAt: '2026-01-12T09:00:00.000Z'
  },
  {
    id: 4,
    title: '[Demo] Security headers review',
    content:
      'Portfolio write-up describing SecurityHeadersFilter (HSTS, CSP, X-Frame-Options) as implemented ' +
      'in the backend filters package. This text is fictional demo content only.',
    createdAt: '2026-01-14T16:45:00.000Z'
  },
  {
    id: 5,
    title: '[Demo] Observability walkthrough',
    content:
      'Synthetic walkthrough of HealthController, MetricsFilter, and CorrelationIdFilter. ' +
      'Intended for the GitHub Pages showcase; not an official CSC or production EXAM document.',
    createdAt: '2026-01-16T11:20:00.000Z'
  },
  {
    id: 6,
    title: '[Demo] Empty-state verification sample',
    content:
      'Extra synthetic report so search and pagination can be exercised. Delete or edit freely; ' +
      'use Reset demo to restore the original fictional dataset.',
    createdAt: '2026-01-18T08:05:00.000Z'
  }
];

/** Versioned, repository-specific sessionStorage key. */
export const DEMO_STORAGE_KEY =
  'exam-architecture-ops-upgrade:demo-reports:v1';

export function cloneSeedReports(): Report[] {
  return DEMO_SEED_REPORTS.map((r) => ({ ...r }));
}

export function isValidReport(value: unknown): value is Report {
  if (!value || typeof value !== 'object') {
    return false;
  }
  const r = value as Record<string, unknown>;
  return (
    typeof r['id'] === 'number' &&
    Number.isFinite(r['id']) &&
    typeof r['title'] === 'string' &&
    typeof r['content'] === 'string' &&
    typeof r['createdAt'] === 'string'
  );
}

export function parseStoredReports(raw: string | null): Report[] | null {
  if (raw == null || raw === '') {
    return null;
  }
  try {
    const parsed: unknown = JSON.parse(raw);
    if (!Array.isArray(parsed) || parsed.length === 0) {
      return null;
    }
    if (!parsed.every(isValidReport)) {
      return null;
    }
    return parsed.map((r) => ({ ...r }));
  } catch {
    return null;
  }
}
