import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProgressService } from './progress.service';
import { ActivatedRoute } from '@angular/router';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-generator-progress',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './progress.component.html',
  styleUrls: ['./progress.component.css']
})
export class ProgressComponent implements OnInit, OnDestroy {
  name = '';
  steps: any[] = [];
  pollingSub?: Subscription;

  constructor(private svc: ProgressService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const n = params.get('name');
      if (n) {
        this.name = n;
        this.load();
        this.pollingSub?.unsubscribe();
        this.pollingSub = interval(3000).subscribe(() => this.load());
      }
    });
  }

  ngOnDestroy(): void {
    this.pollingSub?.unsubscribe();
  }

  load() {
    if (!this.name) return;
    this.svc.getProgress(this.name).subscribe(
      data => { this.steps = Array.isArray(data) ? data : []; },
      err => { this.steps = []; }
    );
  }
}
