/** Matches backend ReportResponse / OpenAPI Report schema. */
export interface Report {
  id: number;
  title: string;
  content: string;
  createdAt: string;
}

export interface PaginationInfo {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}

export interface PaginatedReports {
  data: Report[];
  pagination: PaginationInfo;
}

export interface CreateReportRequest {
  title: string;
  content: string;
}

export interface UpdateReportRequest {
  title: string;
  content: string;
}

export interface ListReportsParams {
  filters?: string;
  page?: number;
  pageSize?: number;
}
