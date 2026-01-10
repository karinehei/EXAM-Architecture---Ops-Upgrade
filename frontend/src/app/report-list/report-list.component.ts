import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface Report {
  id: number;
  title: string;
  content?: string;
  createdAt?: string;
}

@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css']
})
export class ReportListComponent implements OnInit {
  reports: Report[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.isLoading = true;
    this.error = null;
    this.http.get<{ data: Report[] }>('/api/v1/reports').subscribe({
      next: (data: { data: Report[] }) => {
        this.reports = data.data || [];
        this.isLoading = false;
      },
      error: (err: any) => {
        this.error = 'Failed to load reports. Please try again later.';
        this.isLoading = false;
        console.error('Error loading reports:', err);
      }
    });
  }
}
