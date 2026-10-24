import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

export type ScenarioId = 'healthy' | 'db-down' | 'latency';

export interface ServiceState {
  name: string;
  status: 'up' | 'down' | 'degraded';
  detail: string;
}

export interface TimelineEvent {
  time: string;
  message: string;
}

export interface Scenario {
  id: ScenarioId;
  title: string;
  summary: string;
  services: ServiceState[];
  latencyMs: { p50: number; p95: number; p99: number };
  timeline: TimelineEvent[];
  investigateNext: string[];
}

@Component({
  selector: 'app-operations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './operations.component.html',
  styleUrls: ['./operations.component.css']
})
export class OperationsComponent {
  readonly scenarios: Scenario[] = [
    {
      id: 'healthy',
      title: 'Healthy',
      summary:
        'Simulated steady state: API and database responding within expected bounds. Values are illustrative only.',
      services: [
        { name: 'Angular client', status: 'up', detail: 'Static UI reachable' },
        { name: 'Play API', status: 'up', detail: 'Simulated 200 on /health' },
        { name: 'PostgreSQL', status: 'up', detail: 'Simulated connectivity OK' },
        { name: 'Reverse proxy', status: 'up', detail: 'Forwarding normally' }
      ],
      latencyMs: { p50: 45, p95: 120, p99: 210 },
      timeline: [
        { time: '10:00:00', message: 'Synthetic probe: /health → OK' },
        { time: '10:00:15', message: 'Synthetic probe: GET /api/v1/reports → 200 (p95 ~120 ms)' },
        { time: '10:01:00', message: 'No alert thresholds breached (simulated)' }
      ],
      investigateNext: [
        'Continue routine metric review (real stack: MetricsController / logs).',
        'Confirm rate-limit counters are not saturating during peak traffic.'
      ]
    },
    {
      id: 'db-down',
      title: 'Database unavailable',
      summary:
        'Simulated PostgreSQL outage. API health would fail database checks; report queries error. No real health checks are performed here.',
      services: [
        { name: 'Angular client', status: 'up', detail: 'UI still loads (static)' },
        { name: 'Play API', status: 'degraded', detail: 'Process up; data path failing' },
        { name: 'PostgreSQL', status: 'down', detail: 'Simulated connection refused' },
        { name: 'Reverse proxy', status: 'up', detail: 'Forwarding to failing upstream' }
      ],
      latencyMs: { p50: 12, p95: 40, p99: 90 },
      timeline: [
        { time: '10:12:01', message: 'Simulated: DB connection pool errors spike' },
        { time: '10:12:03', message: 'Simulated: GET /api/v1/reports → 500 INTERNAL_ERROR' },
        { time: '10:12:05', message: 'Simulated: /health database check fails' }
      ],
      investigateNext: [
        'On a real deployment: verify PostgreSQL process, network, credentials, and disk.',
        'Inspect Play logs for Slick/JDBC exceptions correlated by X-Request-ID.',
        'Check docker-compose or host service status — do not rely on this browser simulation.'
      ]
    },
    {
      id: 'latency',
      title: 'Elevated API latency',
      summary:
        'Simulated slow report list responses under load. Infrastructure is not modified; numbers are fixed demo values.',
      services: [
        { name: 'Angular client', status: 'up', detail: 'Waiting on slow API (real stack)' },
        { name: 'Play API', status: 'degraded', detail: 'Simulated high request duration' },
        { name: 'PostgreSQL', status: 'degraded', detail: 'Simulated slow queries' },
        { name: 'Reverse proxy', status: 'up', detail: 'Passing through latency' }
      ],
      latencyMs: { p50: 380, p95: 920, p99: 1450 },
      timeline: [
        { time: '11:05:10', message: 'Simulated: p95 crosses 500 ms threshold' },
        { time: '11:05:40', message: 'Simulated: MetricsFilter records elevated durations' },
        { time: '11:06:00', message: 'Simulated: operator reviews slow query / filter plans' }
      ],
      investigateNext: [
        'Compare with documented k6 results under perf/ (repository evidence, not this UI).',
        'On a real stack: check DB indexes, connection pool, and whether LIKE filters are scanning heavily.',
        'Use correlation IDs to trace slow requests through audit and application logs.'
      ]
    }
  ];

  selectedId: ScenarioId = 'healthy';

  get selected(): Scenario {
    return this.scenarios.find((s) => s.id === this.selectedId) ?? this.scenarios[0];
  }

  select(id: ScenarioId): void {
    this.selectedId = id;
  }

  statusLabel(status: ServiceState['status']): string {
    switch (status) {
      case 'up':
        return 'Up (simulated)';
      case 'down':
        return 'Down (simulated)';
      case 'degraded':
        return 'Degraded (simulated)';
    }
  }
}
