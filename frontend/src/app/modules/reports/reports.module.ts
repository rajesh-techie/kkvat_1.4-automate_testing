import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { ReportsRoutingModule } from './reports-routing.module';
import { ReportBuilderComponent } from './components/report-builder/report-builder.component';
import { ReportSchedulerComponent } from './components/report-scheduler/report-scheduler.component';
import { ReportHistoryComponent } from './components/report-history/report-history.component';

import { ReportService } from './services/report.service';
import { ReportViewService } from './services/report-view.service';
import { ReportScheduleService } from './services/report-schedule.service';
import { ReportExecutionService } from './services/report-execution.service';

@NgModule({
  declarations: [
    ReportBuilderComponent,
    ReportSchedulerComponent,
    ReportHistoryComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ReportsRoutingModule
  ],
  providers: [
    ReportService,
    ReportViewService,
    ReportScheduleService,
    ReportExecutionService
  ]
})
export class ReportsModule { }
