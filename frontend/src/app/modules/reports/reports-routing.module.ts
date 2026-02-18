import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportBuilderComponent } from './components/report-builder/report-builder.component';
import { ReportSchedulerComponent } from './components/report-scheduler/report-scheduler.component';
import { ReportHistoryComponent } from './components/report-history/report-history.component';

const routes: Routes = [
  {
    path: 'builder',
    component: ReportBuilderComponent,
    data: { title: 'Report Builder' }
  },
  {
    path: 'scheduler',
    component: ReportSchedulerComponent,
    data: { title: 'Report Scheduler' }
  },
  {
    path: 'history',
    component: ReportHistoryComponent,
    data: { title: 'Report History' }
  },
  {
    path: '',
    redirectTo: 'history',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportsRoutingModule { }
