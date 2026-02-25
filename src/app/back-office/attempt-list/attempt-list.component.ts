import { Component, OnInit } from '@angular/core';
import { Attempt } from '../../models/attempt.model';
import { AttemptService } from '../../services/attempt.service';

@Component({
  selector: 'app-attempt-list',
  templateUrl: './attempt-list.component.html',
  styleUrls: ['./attempt-list.component.scss']
})
export class AttemptListComponent implements OnInit {

  attempts: Attempt[] = [];

  constructor(private attemptService: AttemptService) {}

  ngOnInit(): void {
    this.loadAttempts();
  }

  loadAttempts(): void {
    this.attemptService.getAll().subscribe(data => {
      this.attempts = data.sort((a, b) => {
        const dateA = a.submittedAt ? new Date(a.submittedAt).getTime() : 0;
        const dateB = b.submittedAt ? new Date(b.submittedAt).getTime() : 0;
        return dateB - dateA; // Most recent first
      });
    });
  }

  deleteAttempt(id: number): void {
    if (confirm('Supprimer cette tentative ?')) {
      this.attemptService.delete(id).subscribe(() => this.loadAttempts());
    }
  }

  getScoreColor(attempt: Attempt): string {
    if (!attempt.score || !attempt.quiz) return 'text-gray-500';
    const pct = attempt.score;
    if (pct >= 80) return 'text-green-600';
    if (pct >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
