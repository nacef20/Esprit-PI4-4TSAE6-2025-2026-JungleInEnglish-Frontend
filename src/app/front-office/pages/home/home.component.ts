
import { Component, OnInit } from '@angular/core';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  studentCount = 2500;
  successRate = 98;

  // valeurs affichées (animées)
  studentCountDisplay = 0;
  successRateDisplay = 0;

  ngOnInit(): void {
    this.animateNumber('studentCountDisplay', this.studentCount, 900);
    this.animateNumber('successRateDisplay', this.successRate, 700);
  }

  private animateNumber(key: 'studentCountDisplay' | 'successRateDisplay', target: number, duration = 800) {
    const start = performance.now();
    const from = 0;

    const tick = (now: number) => {
      const t = Math.min(1, (now - start) / duration);
      // easing (smooth)
      const eased = 1 - Math.pow(1 - t, 3);
      (this as any)[key] = Math.floor(from + (target - from) * eased);

      if (t < 1) requestAnimationFrame(tick);
    };

    requestAnimationFrame(tick);
  }

  onGetStarted(): void {
    document.getElementById('pricing')?.scrollIntoView({ behavior: 'smooth' });
  }

onWatchDemo(): void {
  window.open('https://www.tiktok.com/@jungle.in.english/video/7493977706414099718?v=demo', '_blank');
}



videoOpen = false;

openVideo(): void {
  this.videoOpen = true;
}

closeVideo(): void {
  this.videoOpen = false;
}

}
