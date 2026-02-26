import { Component, OnInit } from '@angular/core';
import { Attempt } from '../../models/attempt.model';
import { AttemptService } from '../../services/attempt.service';
import { Quiz } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';

@Component({
  selector: 'app-attempt-list',
  templateUrl: './attempt-list.component.html',
  styleUrls: ['./attempt-list.component.scss']
})
export class AttemptListComponent implements OnInit {

  allAttempts: Attempt[] = [];
  filteredAttempts: Attempt[] = [];
  quizzes: Quiz[] = [];

  selectedQuizId: number | null = null;
  searchStudentId = '';

  // Stats
  totalAttempts = 0;
  avgScore = 0;
  passRate = 0;
  bestScore = 0;

  constructor(
    private attemptService: AttemptService,
    private quizService: QuizService
  ) {}

  ngOnInit(): void {
    this.quizService.getAll().subscribe(data => this.quizzes = data);
    this.loadAttempts();
  }

  loadAttempts(): void {
    this.attemptService.getAll().subscribe(data => {
      this.allAttempts = data.sort((a, b) => {
        const dateA = a.submittedAt ? new Date(a.submittedAt).getTime() : 0;
        const dateB = b.submittedAt ? new Date(b.submittedAt).getTime() : 0;
        return dateB - dateA;
      });
      this.applyFilters();
    });
  }

  applyFilters(): void {
    let result = [...this.allAttempts];

    if (this.selectedQuizId) {
      result = result.filter(a => a.quiz?.id === this.selectedQuizId);
    }

    if (this.searchStudentId.trim()) {
      result = result.filter(a => String(a.studentId).includes(this.searchStudentId.trim()));
    }

    this.filteredAttempts = result;
    this.computeStats();
  }

  computeStats(): void {
    const attempts = this.filteredAttempts;
    this.totalAttempts = attempts.length;

    if (attempts.length === 0) {
      this.avgScore = 0;
      this.passRate = 0;
      this.bestScore = 0;
      return;
    }

    const percentages = attempts.map(a => this.getPercentage(a));
    this.avgScore = Math.round(percentages.reduce((s, p) => s + p, 0) / percentages.length);
    this.passRate = Math.round((percentages.filter(p => p >= 50).length / percentages.length) * 100);
    this.bestScore = Math.max(...percentages);
  }

  getPercentage(attempt: Attempt): number {
    if (!attempt.score && attempt.score !== 0) return 0;
    if (!attempt.totalPoints || attempt.totalPoints === 0) return 0;
    return Math.round((attempt.score / attempt.totalPoints) * 100);
  }

  deleteAttempt(id: number): void {
    if (confirm('Supprimer cette tentative ?')) {
      this.attemptService.delete(id).subscribe(() => this.loadAttempts());
    }
  }

  getScoreColor(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'text-green-600';
    if (pct >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  getScoreBadgeBg(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'bg-green-50';
    if (pct >= 50) return 'bg-yellow-50';
    return 'bg-red-50';
  }

  getPassLabel(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50 ? 'Réussi' : 'Échoué';
  }

  getPassBadgeClasses(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50
      ? 'bg-green-100 text-green-700'
      : 'bg-red-100 text-red-700';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  resetFilters(): void {
    this.selectedQuizId = null;
    this.searchStudentId = '';
    this.applyFilters();
  }
}
