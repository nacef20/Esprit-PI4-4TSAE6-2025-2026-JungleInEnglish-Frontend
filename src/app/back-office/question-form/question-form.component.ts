import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { QuestionService } from '../../services/question.service';

@Component({
  selector: 'app-question-form',
  templateUrl: './question-form.component.html',
  styleUrls: ['./question-form.component.scss']
})
export class QuestionFormComponent implements OnInit {

  questionForm!: FormGroup;
  isEdit = false;
  quizId!: number;
  questionId?: number;
  serverErrors: { [key: string]: string } = {};
  submitting = false;
  generalError = '';

  constructor(
    private fb: FormBuilder,
    private questionService: QuestionService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizId = +this.route.snapshot.params['quizId'];
    this.questionId = +this.route.snapshot.params['questionId'] || undefined;

    this.questionForm = this.fb.group({
      content: [''],
      points: [1]
    });

    if (this.questionId) {
      this.isEdit = true;
      this.questionService.getById(this.questionId).subscribe(question => {
        this.questionForm.patchValue(question);
      });
    }
  }

  onSubmit(): void {
    this.generalError = '';
    this.serverErrors = {};
    this.submitting = true;

    const question = {
      ...this.questionForm.value,
      quiz: { id: this.quizId }
    };

    const request = this.isEdit && this.questionId
      ? this.questionService.update(this.questionId, question)
      : this.questionService.create(question);

    request.subscribe({
      next: () => this.router.navigate(['/back/quizzes', this.quizId, 'questions']),
      error: (err) => {
        this.submitting = false;
        if (err.error?.fieldErrors) {
          this.serverErrors = err.error.fieldErrors;
        } else {
          this.generalError = err.error?.error || err.message || 'An unexpected error occurred. Please try again.';
        }
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/back/quizzes', this.quizId, 'questions']);
  }
}
