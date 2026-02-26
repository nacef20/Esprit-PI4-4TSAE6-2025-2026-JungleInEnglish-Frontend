import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Quiz, QuizStatus } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';
import { AttemptService } from '../../services/attempt.service';

@Component({
  selector: 'app-quiz-student-list',
  templateUrl: './quiz-student-list.component.html',
  styleUrls: ['./quiz-student-list.component.scss']
})
export class QuizStudentListComponent implements OnInit {

  quizzes: Quiz[] = [];
  attemptCounts: Map<number, number> = new Map();
  studentId = 1; // placeholder — no auth

  constructor(
    private quizService: QuizService,
    private attemptService: AttemptService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizService.getAll().subscribe(data => {
      this.quizzes = data.filter(q => q.status === 'PUBLISHED' || q.status === 'CLOSED');
      // Load attempt counts for each quiz
      this.quizzes.forEach(quiz => {
        if (quiz.id) {
          this.attemptService.countByQuizAndStudent(quiz.id, this.studentId).subscribe(count => {
            this.attemptCounts.set(quiz.id!, count);
          });
        }
      });
    });
  }

  canTakeQuiz(quiz: Quiz): boolean {
    if (quiz.status === 'CLOSED') return false;
    if (quiz.maxAttempts && quiz.maxAttempts > 0) {
      const count = this.attemptCounts.get(quiz.id!) || 0;
      return count < quiz.maxAttempts;
    }
    return true;
  }

  getQuizBlockReason(quiz: Quiz): string {
    if (quiz.status === 'CLOSED') return 'Quiz fermé';
    if (quiz.maxAttempts && quiz.maxAttempts > 0) {
      const count = this.attemptCounts.get(quiz.id!) || 0;
      if (count >= quiz.maxAttempts) return 'Nombre max de tentatives atteint';
    }
    return '';
  }

  startQuiz(quiz: Quiz): void {
    if (!this.canTakeQuiz(quiz)) return;
    this.router.navigate(['/front/quiz', quiz.id]);
  }
}
