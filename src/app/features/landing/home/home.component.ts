import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  studentCount = 2500;
  successRate = 98;
  studentCountDisplay = 0;
  successRateDisplay = 0;

  ngOnInit(): void {
    if (typeof requestAnimationFrame === 'undefined') {
      this.studentCountDisplay = this.studentCount;
      this.successRateDisplay = this.successRate;
      return;
    }
    this.animateNumber('studentCountDisplay', this.studentCount, 900);
    this.animateNumber('successRateDisplay', this.successRate, 700);
  }

  private animateNumber(
    key: 'studentCountDisplay' | 'successRateDisplay',
    target: number,
    duration = 800
  ) {
    const start = performance.now();
    const from = 0;
    const tick = (now: number) => {
      const t = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      (this as unknown as Record<string, number>)[key] = Math.floor(
        from + (target - from) * eased
      );
      if (t < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }

  onGetStarted(): void {
    document.getElementById('pricing')?.scrollIntoView({ behavior: 'smooth' });
  }
}
