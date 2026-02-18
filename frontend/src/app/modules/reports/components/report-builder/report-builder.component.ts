import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ReportService } from '../../services/report.service';
import { ReportViewService } from '../../services/report-view.service';
import {
  ReportView,
  ReportViewField,
  ReportRequest,
  FilterCondition,
  SortConfig
} from '../../models/report.model';

@Component({
  selector: 'app-report-builder',
  templateUrl: './report-builder.component.html',
  styleUrls: ['./report-builder.component.css']
})
export class ReportBuilderComponent implements OnInit {
  reportForm!: FormGroup;
  currentStep = 1;
  totalSteps = 5;
  
  // Data
  views: ReportView[] = [];
  allFields: ReportViewField[] = [];
  filterableFields: ReportViewField[] = [];
  sortableFields: ReportViewField[] = [];
  selectedColumns: Set<string> = new Set();
  filters: FilterCondition[] = [];
  sortConfig: SortConfig[] = [];
  
  // UI State
  loading = false;
  saving = false;
  error: string | null = null;
  success: string | null = null;
  selectedViewId: number | null = null;
  
  // Step 5 Preview
  previewData: any[] = [];
  previewLoading = false;

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private reportViewService: ReportViewService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadReportViews();
  }

  initializeForm(): void {
    this.reportForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      viewId: ['', Validators.required],
      reportType: ['EXECUTION', Validators.required],
      isPublic: [false]
    });
  }

  // Step 1: Load Report Views
  loadReportViews(): void {
    this.loading = true;
    this.reportViewService.getAllViews().subscribe(
      (views) => {
        this.views = views;
        this.loading = false;
      },
      (error) => {
        this.error = 'Failed to load report views';
        this.loading = false;
      }
    );
  }

  onViewSelected(viewId: number): void {
    this.selectedViewId = viewId;
    this.reportForm.patchValue({ viewId });
    this.loadViewFields(viewId);
  }

  // Step 2: Load and display available fields
  loadViewFields(viewId: number): void {
    this.loading = true;
    this.reportViewService.getViewFields(viewId).subscribe(
      (fields) => {
        this.allFields = fields;
        this.selectedColumns.clear();
        this.loading = false;
      },
      (error) => {
        this.error = 'Failed to load view fields';
        this.loading = false;
      }
    );

    this.loadFilterableFields(viewId);
    this.loadSortableFields(viewId);
  }

  loadFilterableFields(viewId: number): void {
    this.reportViewService.getFilterableFields(viewId).subscribe(
      (fields) => {
        this.filterableFields = fields;
      }
    );
  }

  loadSortableFields(viewId: number): void {
    this.reportViewService.getSortableFields(viewId).subscribe(
      (fields) => {
        this.sortableFields = fields;
      }
    );
  }

  toggleColumn(fieldName: string): void {
    if (this.selectedColumns.has(fieldName)) {
      this.selectedColumns.delete(fieldName);
    } else {
      this.selectedColumns.add(fieldName);
    }
  }

  isColumnSelected(fieldName: string): boolean {
    return this.selectedColumns.has(fieldName);
  }

  // Step 3: Filter Management
  addFilter(): void {
    this.filters.push({
      field: this.filterableFields[0]?.fieldName || '',
      operator: '=',
      value: ''
    });
  }

  removeFilter(index: number): void {
    this.filters.splice(index, 1);
  }

  updateFilter(index: number, property: string, value: any): void {
    this.filters[index] = { ...this.filters[index], [property]: value };
  }

  // Step 4: Sort Configuration
  addSort(): void {
    this.sortConfig.push({
      field: this.sortableFields[0]?.fieldName || '',
      direction: 'ASC'
    });
  }

  removeSort(index: number): void {
    this.sortConfig.splice(index, 1);
  }

  updateSort(index: number, property: string, value: any): void {
    this.sortConfig[index] = { ...this.sortConfig[index], [property]: value };
  }

  moveSortUp(index: number): void {
    if (index > 0) {
      [this.sortConfig[index], this.sortConfig[index - 1]] = [
        this.sortConfig[index - 1],
        this.sortConfig[index]
      ];
    }
  }

  moveSortDown(index: number): void {
    if (index < this.sortConfig.length - 1) {
      [this.sortConfig[index], this.sortConfig[index + 1]] = [
        this.sortConfig[index + 1],
        this.sortConfig[index]
      ];
    }
  }

  // Step 5: Preview and Save
  previewReport(): void {
    if (!this.selectedViewId) {
      this.error = 'Please select a data source first';
      return;
    }

    if (this.selectedColumns.size === 0) {
      this.error = 'Please select at least one column';
      return;
    }

    // TODO: Implement preview logic by calling an API endpoint
    this.previewLoading = true;
    setTimeout(() => {
      this.previewData = [
        { sample: 'Sample data would appear here after backend implementation' }
      ];
      this.previewLoading = false;
    }, 1000);
  }

  saveReport(): void {
    if (!this.reportForm.valid) {
      this.error = 'Please fill in all required fields';
      return;
    }

    if (this.selectedColumns.size === 0) {
      this.error = 'Please select at least one column';
      return;
    }

    const reportRequest: ReportRequest = {
      name: this.reportForm.value.name,
      description: this.reportForm.value.description,
      viewId: this.reportForm.value.viewId,
      selectedColumns: Array.from(this.selectedColumns),
      filterConditions: this.filters.length > 0 ? { conditions: this.filters } : undefined,
      sortConfig: this.sortConfig.length > 0 ? this.sortConfig : undefined,
      reportType: this.reportForm.value.reportType,
      isPublic: this.reportForm.value.isPublic
    };

    this.saving = true;
    this.reportService.createReport(reportRequest).subscribe(
      (response) => {
        this.success = `Report "${response.name}" created successfully!`;
        this.saving = false;
        setTimeout(() => {
          this.router.navigate(['/reports/list']);
        }, 2000);
      },
      (error) => {
        this.error = error.error?.message || 'Failed to save report';
        this.saving = false;
      }
    );
  }

  // Navigation
  nextStep(): void {
    if (this.currentStep < this.totalSteps) {
      if (this.validateStep(this.currentStep)) {
        this.currentStep++;
      }
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  validateStep(step: number): boolean {
    switch (step) {
      case 1:
        return this.reportForm.get('name')?.valid && this.reportForm.get('viewId')?.valid;
      case 2:
        return this.selectedColumns.size > 0;
      default:
        return true;
    }
  }

  getStepTitle(): string {
    const titles = [
      'Report Details',
      'Select Columns',
      'Add Filters',
      'Configure Sorting',
      'Preview & Save'
    ];
    return titles[this.currentStep - 1];
  }
}
