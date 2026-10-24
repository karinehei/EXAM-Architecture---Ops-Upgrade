import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

export type ImplStatus = 'implemented' | 'planned' | 'tooling';

export interface ArchNode {
  id: string;
  label: string;
  kind: 'runtime' | 'tooling';
  status: ImplStatus;
  responsibility: string;
  paths: string[];
  notes?: string;
}

@Component({
  selector: 'app-architecture',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './architecture.component.html',
  styleUrls: ['./architecture.component.css']
})
export class ArchitectureComponent {
  readonly nodes: ArchNode[] = [
    {
      id: 'frontend',
      label: 'Angular frontend',
      kind: 'runtime',
      status: 'implemented',
      responsibility:
        'Standalone Angular UI that lists and manages reports via HTTP in normal builds. This portfolio demo replaces HTTP with an in-browser store.',
      paths: [
        'frontend/src/main.ts',
        'frontend/src/app/report-list/',
        'frontend/src/app/services/'
      ]
    },
    {
      id: 'proxy',
      label: 'Reverse proxy',
      kind: 'runtime',
      status: 'implemented',
      responsibility:
        'Terminates client traffic and forwards API calls. Local Docker Compose uses nginx for the frontend container; production notes reference Apache.',
      paths: ['frontend/nginx.conf', 'docker-compose.yml', 'README.md']
    },
    {
      id: 'auth',
      label: 'Authentication boundary',
      kind: 'runtime',
      status: 'implemented',
      responsibility:
        'Production intends Shibboleth/HAKA. Locally, DevAuthFilter can inject mock identity headers when DEV_AUTH_ENABLED=true — never for this public demo.',
      paths: [
        'backend/app/filters/DevAuthFilter.scala',
        'docs/security/README.md'
      ],
      notes:
        'Browser simulations on the Operations page are not authentication or authorization.'
    },
    {
      id: 'api',
      label: 'Play / Scala REST API',
      kind: 'runtime',
      status: 'implemented',
      responsibility:
        'Report CRUD at /api/v1/reports with pagination and filters. Filters chain correlation IDs, security headers, rate limits, audit logging, optional dev auth, and metrics.',
      paths: [
        'backend/app/controllers/ReportController.scala',
        'backend/app/ExamApplicationLoader.scala',
        'backend/conf/routes'
      ]
    },
    {
      id: 'filters',
      label: 'Backend HTTP filters',
      kind: 'runtime',
      status: 'implemented',
      responsibility:
        'CorrelationIdFilter, SecurityHeadersFilter, RateLimitFilter, AuditLogFilter, DevAuthFilter, MetricsFilter — registered in ExamApplicationLoader.',
      paths: [
        'backend/app/filters/',
        'backend/app/ExamApplicationLoader.scala'
      ]
    },
    {
      id: 'db',
      label: 'PostgreSQL',
      kind: 'runtime',
      status: 'implemented',
      responsibility:
        'Persists reports via Slick ReportRepository. Schema and migrations live under database/; Play evolutions under backend/conf/evolutions.',
      paths: [
        'backend/app/repositories/ReportRepository.scala',
        'database/schema.sql',
        'backend/conf/evolutions/default/1.sql'
      ]
    },
    {
      id: 'observability',
      label: 'Health & metrics',
      kind: 'runtime',
      status: 'implemented',
      responsibility:
        'HealthController and MetricsController expose operational endpoints used outside this static demo.',
      paths: [
        'backend/app/controllers/HealthController.scala',
        'backend/app/controllers/MetricsController.scala',
        'docs/observability/'
      ]
    },
    {
      id: 'ci',
      label: 'CI / Docker / k6',
      kind: 'tooling',
      status: 'tooling',
      responsibility:
        'Build and verification tooling — not request-path services. GitHub Actions CI, Docker images, and k6 load scripts support development and ops evidence.',
      paths: [
        '.github/workflows/ci.yml',
        'perf/load-test.js',
        'backend/Dockerfile',
        'frontend/Dockerfile'
      ]
    },
    {
      id: 'c4',
      label: 'C4 diagrams (docs)',
      kind: 'tooling',
      status: 'planned',
      responsibility:
        'docs/architecture/README.md marks context, container, and component C4 diagram files as Planned — not shipped as rendered diagrams in-repo.',
      paths: ['docs/architecture/README.md']
    }
  ];

  selectedId = 'frontend';

  get selected(): ArchNode {
    return this.nodes.find((n) => n.id === this.selectedId) ?? this.nodes[0];
  }

  select(id: string): void {
    this.selectedId = id;
  }

  statusLabel(status: ImplStatus): string {
    switch (status) {
      case 'implemented':
        return 'Implemented in repository';
      case 'planned':
        return 'Documented as planned';
      case 'tooling':
        return 'Build / deployment tooling';
    }
  }
}
