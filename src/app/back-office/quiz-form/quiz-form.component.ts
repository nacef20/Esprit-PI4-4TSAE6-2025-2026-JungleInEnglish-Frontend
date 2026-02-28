import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService } from '../../services/quiz.service';
import { QuizStatus } from '../../models/quiz.model';

@Component({
  selector: 'app-quiz-form',
  templateUrl: './quiz-form.component.html',
  styleUrls: ['./quiz-form.component.scss']
})
export class QuizFormComponent implements OnInit {

  quizForm!: FormGroup;
  isEdit = false;
  quizId?: number;
  statuses = Object.values(QuizStatus);
  serverErrors: { [key: string]: string } = {};
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private quizService: QuizService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizForm = this.fb.group({
      title: [''],
      description: [''],
      duration: [30],
      status: [QuizStatus.DRAFT],
      teacherId: [null],
      courseId: [null]
    });

    this.quizId = +this.route.snapshot.params['id'];
    if (this.quizId) {
      this.isEdit = true;
      this.quizService.getById(this.quizId).subscribe(quiz => {
        this.quizForm.patchValue(quiz);
      });
    }
  }

  onSubmit(): void {
    this.serverErrors = {};
    this.submitting = true;

    const quiz = this.quizForm.value;
    const request = this.isEdit && this.quizId
      ? this.quizService.update(this.quizId, quiz)
      : this.quizService.create(quiz);

    request.subscribe({
      next: () => this.router.navigate(['/back/quizzes']),
      error: (err) => {
        this.submitting = false;
        if (err.error?.fieldErrors) {
          this.serverErrors = err.error.fieldErrors;
        }
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/back/quizzes']);
  }
}
