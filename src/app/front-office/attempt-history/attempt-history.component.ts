import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Attempt } from '../../models/attempt.model';
import { AttemptService } from '../../services/attempt.service';

@Component({
  selector: 'app-attempt-history',
  templateUrl: './attempt-history.component.html',
  styleUrls: ['./attempt-history.component.scss']
})
export class AttemptHistoryComponent implements OnInit {

  attempts: Attempt[] = [];

  constructor(
    private attemptService: AttemptService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAttempts();
  }

  loadAttempts(): void {
    // Filter by studentId = 1 placeholder (no auth system)
    this.attemptService.getAll().subscribe(data => {
      this.attempts = data
        .filter(a => a.studentId === 1)
        .sort((a, b) => {
          const dateA = a.submittedAt ? new Date(a.submittedAt).getTime() : 0;
          const dateB = b.submittedAt ? new Date(b.submittedAt).getTime() : 0;
          return dateB - dateA;
        });
    });
  }

  getScorePercentage(attempt: Attempt): number {
    return attempt.score ?? 0;
  }

  getScoreColor(attempt: Attempt): string {
    const score = attempt.score ?? 0;
    if (score >= 80) return 'text-green-600';
    if (score >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  getScoreBg(attempt: Attempt): string {
    const score = attempt.score ?? 0;
    if (score >= 80) return 'bg-green-50 border-green-200';
    if (score >= 50) return 'bg-yellow-50 border-yellow-200';
    return 'bg-red-50 border-red-200';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  retakeQuiz(quizId?: number): void {
    if (quizId) {
      this.router.navigate(['/front/quiz', quizId]);
    }
  }

  goToQuizzes(): void {
    this.router.navigate(['/front/quizzes']);
  }
}
