import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../services/report.service';
import { Report, PaginationInfo } from '../models/report.model';
import { environment } from '../../environments/environment';

type FormMode = 'create' | 'edit' | null;

@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ReportListComponent implements OnInit {
  readonly isDemo = environment.demo;

  reports: Report[] = [];
  pagination: PaginationInfo | null = null;
  selected: Report | null = null;

  searchQuery = '';
  page = 1;
  readonly pageSize = 5;

  isLoading = true;
  isSaving = false;
  error: string | null = null;
  successMessage: string | null = null;
  statusMessage = '';

  formMode: FormMode = null;
  formTitle = '';
  formContent = '';
  formTitleError = '';
  formContentError = '';

  deleteTarget: Report | null = null;

  @ViewChild('dialogTitle') dialogTitleRef?: ElementRef<HTMLElement>;
  @ViewChild('deleteDialogTitle') deleteDialogTitleRef?: ElementRef<HTMLElement>;

  private previousFocus: HTMLElement | null = null;

  constructor(
    private readonly reportService: ReportService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.isLoading = true;
    this.error = null;
    this.reportService
      .list({
        filters: this.searchQuery.trim() || undefined,
        page: this.page,
        pageSize: this.pageSize
      })
      .subscribe({
        next: (response) => {
          this.reports = response.data || [];
          this.pagination = response.pagination;
          this.isLoading = false;
          this.reconcileSelection();
          this.announce(
            `Loaded ${this.reports.length} of ${response.pagination.total} reports`
          );
          this.cdr.detectChanges();
        },
        error: (err: { status?: number; message?: string }) => {
          this.error = `Failed to load reports: ${err.status ?? err.message ?? 'Unknown error'}. Please try again later.`;
          this.isLoading = false;
          this.reports = [];
          this.pagination = null;
          this.cdr.detectChanges();
        }
      });
  }

  onSearchSubmit(): void {
    this.page = 1;
    this.clearMessages();
    this.loadReports();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.page = 1;
    this.clearMessages();
    this.loadReports();
  }

  goToPage(nextPage: number): void {
    if (!this.pagination) {
      return;
    }
    if (nextPage < 1 || nextPage > this.pagination.totalPages) {
      return;
    }
    this.page = nextPage;
    this.loadReports();
  }

  selectReport(report: Report): void {
    this.selected = report;
    this.announce(`Selected report: ${report.title}`);
  }

  openCreate(): void {
    this.clearMessages();
    this.formMode = 'create';
    this.formTitle = '';
    this.formContent = '';
    this.formTitleError = '';
    this.formContentError = '';
    this.trapFocus(() => this.dialogTitleRef?.nativeElement);
  }

  openEdit(report: Report): void {
    this.clearMessages();
    this.selected = report;
    this.formMode = 'edit';
    this.formTitle = report.title;
    this.formContent = report.content;
    this.formTitleError = '';
    this.formContentError = '';
    this.trapFocus(() => this.dialogTitleRef?.nativeElement);
  }

  closeForm(): void {
    this.formMode = null;
    this.formTitleError = '';
    this.formContentError = '';
    this.restoreFocus();
  }

  submitForm(): void {
    if (!this.validateForm()) {
      return;
    }
    this.isSaving = true;
    this.error = null;

    const payload = {
      title: this.formTitle.trim(),
      content: this.formContent.trim()
    };

    if (this.formMode === 'create') {
      this.reportService.create(payload).subscribe({
        next: (created) => {
          this.isSaving = false;
          this.closeForm();
          this.successMessage = `Created “${created.title}”.`;
          this.selected = created;
          this.page = 1;
          this.loadReports();
        },
        error: (err: { status?: number; message?: string }) => {
          this.isSaving = false;
          this.error = err.message ?? 'Failed to create report.';
          this.cdr.detectChanges();
        }
      });
      return;
    }

    if (this.formMode === 'edit' && this.selected) {
      const id = this.selected.id;
      this.reportService.update(id, payload).subscribe({
        next: (updated) => {
          this.isSaving = false;
          this.closeForm();
          this.successMessage = `Updated “${updated.title}”.`;
          this.selected = updated;
          this.loadReports();
        },
        error: (err: { status?: number; message?: string }) => {
          this.isSaving = false;
          this.error = err.message ?? 'Failed to update report.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  confirmDelete(report: Report): void {
    this.deleteTarget = report;
    this.trapFocus(() => this.deleteDialogTitleRef?.nativeElement);
  }

  cancelDelete(): void {
    this.deleteTarget = null;
    this.restoreFocus();
  }

  performDelete(): void {
    if (!this.deleteTarget) {
      return;
    }
    const target = this.deleteTarget;
    this.isSaving = true;
    this.reportService.delete(target.id).subscribe({
      next: () => {
        this.isSaving = false;
        this.deleteTarget = null;
        this.restoreFocus();
        this.successMessage = `Deleted “${target.title}”.`;
        if (this.selected?.id === target.id) {
          this.selected = null;
        }
        // Keep page consistent if last item on page removed
        if (this.reports.length <= 1 && this.page > 1) {
          this.page -= 1;
        }
        this.loadReports();
      },
      error: (err: { status?: number; message?: string }) => {
        this.isSaving = false;
        this.deleteTarget = null;
        this.restoreFocus();
        this.error = err.message ?? 'Failed to delete report.';
        this.cdr.detectChanges();
      }
    });
  }

  resetDemo(): void {
    if (!this.isDemo || !this.reportService.resetDemo) {
      return;
    }
    this.clearMessages();
    this.formMode = null;
    this.deleteTarget = null;
    this.selected = null;
    this.searchQuery = '';
    this.page = 1;
    this.isSaving = true;
    this.reportService.resetDemo().subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = 'Demo data restored to the original fictional dataset.';
        this.loadReports();
      },
      error: (err: { message?: string }) => {
        this.isSaving = false;
        this.error = err.message ?? 'Failed to reset demo.';
        this.cdr.detectChanges();
      }
    });
  }

  onDialogKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      if (this.deleteTarget) {
        this.cancelDelete();
      } else if (this.formMode) {
        this.closeForm();
      }
    }
  }

  get totalPages(): number {
    return this.pagination?.totalPages ?? 0;
  }

  get formHeading(): string {
    return this.formMode === 'create' ? 'Create report' : 'Edit report';
  }

  private validateForm(): boolean {
    this.formTitleError = '';
    this.formContentError = '';
    let ok = true;
    if (!this.formTitle.trim()) {
      this.formTitleError = 'Title is required.';
      ok = false;
    } else if (this.formTitle.trim().length > 200) {
      this.formTitleError = 'Title must be 200 characters or fewer.';
      ok = false;
    }
    if (!this.formContent.trim()) {
      this.formContentError = 'Content is required.';
      ok = false;
    }
    return ok;
  }

  private reconcileSelection(): void {
    if (!this.selected) {
      return;
    }
    const stillVisible = this.reports.find((r) => r.id === this.selected!.id);
    if (stillVisible) {
      this.selected = stillVisible;
      return;
    }
    // Selection may be on another page or deleted — refresh from list or clear
    this.reportService.get(this.selected.id).subscribe({
      next: (report) => {
        this.selected = report;
        this.cdr.detectChanges();
      },
      error: () => {
        this.selected = null;
        this.cdr.detectChanges();
      }
    });
  }

  private clearMessages(): void {
    this.error = null;
    this.successMessage = null;
  }

  private announce(message: string): void {
    this.statusMessage = message;
  }

  private trapFocus(getTitle: () => HTMLElement | undefined): void {
    this.previousFocus = document.activeElement as HTMLElement;
    setTimeout(() => {
      getTitle()?.focus();
      this.cdr.detectChanges();
    });
  }

  private restoreFocus(): void {
    setTimeout(() => {
      this.previousFocus?.focus();
      this.previousFocus = null;
    });
  }
}
