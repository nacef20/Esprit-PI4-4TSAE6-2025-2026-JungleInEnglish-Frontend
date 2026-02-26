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
  studentId = 1; // placeholder — no auth

  // Stats
  totalAttempts = 0;
  avgScore = 0;
  bestScore = 0;
  passRate = 0;

  constructor(
    private attemptService: AttemptService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAttempts();
  }

  loadAttempts(): void {
    this.attemptService.getAll().subscribe(data => {
      this.attempts = data
        .filter(a => a.studentId === this.studentId)
        .sort((a, b) => {
          const dateA = a.submittedAt ? new Date(a.submittedAt).getTime() : 0;
          const dateB = b.submittedAt ? new Date(b.submittedAt).getTime() : 0;
          return dateB - dateA;
        });
      this.computeStats();
    });
  }

  computeStats(): void {
    this.totalAttempts = this.attempts.length;
    if (this.attempts.length === 0) {
      this.avgScore = 0;
      this.bestScore = 0;
      this.passRate = 0;
      return;
    }
    const percentages = this.attempts.map(a => this.getPercentage(a));
    this.avgScore = Math.round(percentages.reduce((s, p) => s + p, 0) / percentages.length);
    this.bestScore = Math.max(...percentages);
    this.passRate = Math.round((percentages.filter(p => p >= 50).length / percentages.length) * 100);
  }

  getPercentage(attempt: Attempt): number {
    if (!attempt.score && attempt.score !== 0) return 0;
    if (!attempt.totalPoints || attempt.totalPoints === 0) return 0;
    return Math.round((attempt.score / attempt.totalPoints) * 100);
  }

  getScoreColor(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'text-green-600';
    if (pct >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  getScoreBg(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'bg-green-50 border-green-200';
    if (pct >= 50) return 'bg-yellow-50 border-yellow-200';
    return 'bg-red-50 border-red-200';
  }

  getPassLabel(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50 ? 'Réussi' : 'Échoué';
  }

  getPassBadge(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50
      ? 'bg-green-100 text-green-700'
      : 'bg-red-100 text-red-700';
  }

  getProgressBarColor(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'bg-green-500';
    if (pct >= 50) return 'bg-yellow-500';
    return 'bg-red-500';
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

  viewDetail(attemptId: number): void {
    this.router.navigate(['/front/my-attempts', attemptId]);
  }

  goToQuizzes(): void {
    this.router.navigate(['/front/quizzes']);
  }
}
