import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Quiz } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';

@Component({
  selector: 'app-quiz-student-list',
  templateUrl: './quiz-student-list.component.html',
  styleUrls: ['./quiz-student-list.component.scss']
})
export class QuizStudentListComponent implements OnInit {

  quizzes: Quiz[] = [];

  constructor(private quizService: QuizService, private router: Router) {}

  ngOnInit(): void {
    this.quizService.getAll().subscribe(data => {
      this.quizzes = data.filter(q => q.status === 'PUBLISHED');
    });
  }

  startQuiz(quizId: number): void {
    this.router.navigate(['/front/quiz', quizId]);
  }
}
