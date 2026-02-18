import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { ReportScheduleService } from '../../services/report-schedule.service';
import { ReportResponse, ReportSchedule, ReportScheduleRequest } from '../../models/report.model';

@Component({
  selector: 'app-report-scheduler',
  templateUrl: './report-scheduler.component.html',
  styleUrls: ['./report-scheduler.component.css']
})
export class ReportSchedulerComponent implements OnInit {
  scheduleForm!: FormGroup;
  schedules: ReportSchedule[] = [];
  reports: ReportResponse[] = [];
  
  // UI State
  loading = false;
  saving = false;
  error: string | null = null;
  success: string | null = null;
  showForm = false;
  editingId: number | null = null;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalSchedules = 0;
  
  // Frequency options
  frequencies = ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY'];
  daysOfWeek = [
    { value: 0, label: 'Sunday' },
    { value: 1, label: 'Monday' },
    { value: 2, label: 'Tuesday' },
    { value: 3, label: 'Wednesday' },
    { value: 4, label: 'Thursday' },
    { value: 5, label: 'Friday' },
    { value: 6, label: 'Saturday' }
  ];

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private scheduleService: ReportScheduleService
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadReports();
    this.loadSchedules();
  }

  initializeForm(): void {
    this.scheduleForm = this.fb.group({
      reportId: ['', Validators.required],
      scheduleName: ['', [Validators.required, Validators.minLength(3)]],
      frequency: ['DAILY', Validators.required],
      dayOfWeek: [null],
      dayOfMonth: [null],
      timeOfDay: ['09:00', Validators.required],
      emailRecipients: [''],
      isActive: [true]
    });
  }

  loadReports(): void {
    this.reportService.getAllReports(0, 100).subscribe(
      (response) => {
        this.reports = response.content;
      },
      (error) => {
        this.error = 'Failed to load reports';
      }
    );
  }

  loadSchedules(): void {
    this.loading = true;
    this.scheduleService.getAllSchedules(this.currentPage, this.pageSize).subscribe(
      (response) => {
        this.schedules = response.content;
        this.totalSchedules = response.totalElements;
        this.loading = false;
      },
      (error) => {
        this.error = 'Failed to load schedules';
        this.loading = false;
      }
    );
  }

  onFrequencyChange(frequency: string): void {
    const dayOfWeekControl = this.scheduleForm.get('dayOfWeek');
    const dayOfMonthControl = this.scheduleForm.get('dayOfMonth');

    // Reset validators based on frequency
    if (frequency === 'WEEKLY') {
      dayOfWeekControl?.setValidators(Validators.required);
      dayOfMonthControl?.clearValidators();
    } else if (['MONTHLY', 'QUARTERLY', 'ANNUALLY'].includes(frequency)) {
      dayOfMonthControl?.setValidators([Validators.required, Validators.min(1), Validators.max(31)]);
      dayOfWeekControl?.clearValidators();
    } else {
      dayOfWeekControl?.clearValidators();
      dayOfMonthControl?.clearValidators();
    }

    dayOfWeekControl?.updateValueAndValidity();
    dayOfMonthControl?.updateValueAndValidity();
  }

  openForm(): void {
    this.showForm = true;
    this.editingId = null;
    this.scheduleForm.reset({ frequency: 'DAILY', isActive: true });
    this.error = null;
    this.success = null;
  }

  editSchedule(schedule: ReportSchedule): void {
    this.showForm = true;
    this.editingId = schedule.id;
    this.scheduleForm.patchValue({
      reportId: schedule.reportId,
      scheduleName: schedule.scheduleName,
      frequency: schedule.frequency,
      dayOfWeek: schedule.dayOfWeek,
      dayOfMonth: schedule.dayOfMonth,
      timeOfDay: this.formatTimeForInput(schedule.timeOfDay),
      emailRecipients: schedule.emailRecipients,
      isActive: schedule.isActive
    });
    this.onFrequencyChange(schedule.frequency);
    this.error = null;
    this.success = null;
  }

  saveSchedule(): void {
    if (!this.scheduleForm.valid) {
      this.error = 'Please fill in all required fields';
      return;
    }

    const scheduleRequest: ReportScheduleRequest = {
      reportId: this.scheduleForm.value.reportId,
      scheduleName: this.scheduleForm.value.scheduleName,
      frequency: this.scheduleForm.value.frequency,
      dayOfWeek: this.scheduleForm.value.dayOfWeek,
      dayOfMonth: this.scheduleForm.value.dayOfMonth,
      timeOfDay: this.scheduleForm.value.timeOfDay + ':00',
      emailRecipients: this.scheduleForm.value.emailRecipients,
      isActive: this.scheduleForm.value.isActive
    };

    this.saving = true;

    if (this.editingId) {
      this.scheduleService.updateSchedule(this.editingId, scheduleRequest).subscribe(
        (response) => {
          this.success = `Schedule "${response.scheduleName}" updated successfully!`;
          this.saving = false;
          this.showForm = false;
          this.loadSchedules();
        },
        (error) => {
          this.error = error.error?.message || 'Failed to update schedule';
          this.saving = false;
        }
      );
    } else {
      this.scheduleService.createSchedule(scheduleRequest).subscribe(
        (response) => {
          this.success = `Schedule "${response.scheduleName}" created successfully!`;
          this.saving = false;
          this.showForm = false;
          this.loadSchedules();
        },
        (error) => {
          this.error = error.error?.message || 'Failed to create schedule';
          this.saving = false;
        }
      );
    }
  }

  deleteSchedule(id: number): void {
    if (confirm('Are you sure you want to delete this schedule?')) {
      this.scheduleService.deleteSchedule(id).subscribe(
        () => {
          this.success = 'Schedule deleted successfully!';
          this.loadSchedules();
        },
        (error) => {
          this.error = 'Failed to delete schedule';
        }
      );
    }
  }

  cancelForm(): void {
    this.showForm = false;
    this.scheduleForm.reset({ frequency: 'DAILY', isActive: true });
    this.error = null;
  }

  getReportName(reportId: number): string {
    return this.reports.find(r => r.id === reportId)?.name || 'Unknown';
  }

  formatTimeForInput(time: string): string {
    // Assuming time format is HH:mm:ss, convert to HH:mm
    return time.substring(0, 5);
  }

  formatNextExecution(date: Date | undefined): string {
    if (!date) return 'Not scheduled';
    return new Date(date).toLocaleString();
  }

  getFrequencyDisplay(frequency: string): string {
    const displays: any = {
      DAILY: 'Every Day',
      WEEKLY: 'Weekly',
      MONTHLY: 'Monthly',
      QUARTERLY: 'Quarterly',
      ANNUALLY: 'Annually'
    };
    return displays[frequency] || frequency;
  }

  nextPage(): void {
    if (this.currentPage < Math.ceil(this.totalSchedules / this.pageSize) - 1) {
      this.currentPage++;
      this.loadSchedules();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadSchedules();
    }
  }

  get hasNextPage(): boolean {
    return this.currentPage < Math.ceil(this.totalSchedules / this.pageSize) - 1;
  }

  get hasPreviousPage(): boolean {
    return this.currentPage > 0;
  }
}
