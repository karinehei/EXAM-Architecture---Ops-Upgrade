import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  styleUrls: ['./report-list.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class ReportListComponent implements OnInit {
  reports: Report[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.isLoading = true;
    this.error = null;
    console.log('Loading reports from /api/v1/reports...');
    this.http.get<{ data: Report[]; pagination: any }>('/api/v1/reports').subscribe({
      next: (response: { data: Report[]; pagination: any }) => {
        console.log('Reports loaded successfully:', response);
        this.reports = response.data || [];
        this.isLoading = false;
        this.cdr.detectChanges(); // Force change detection
        console.log('Reports array:', this.reports);
        console.log('isLoading set to:', this.isLoading);
      },
      error: (err: any) => {
        console.error('Error loading reports:', err);
        console.error('Error details:', {
          status: err.status,
          statusText: err.statusText,
          message: err.message,
          error: err.error
        });
        this.error = `Failed to load reports: ${err.status || 'Unknown error'}. Please try again later.`;
        this.isLoading = false;
      }
    });
  }
}
