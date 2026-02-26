import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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

  constructor(
    private fb: FormBuilder,
    private quizService: QuizService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      duration: [30, Validators.required],
      status: [QuizStatus.DRAFT, Validators.required],
      teacherId: [null],
      courseId: [null],
      maxAttempts: [null]
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
    if (this.quizForm.invalid) return;

    const quiz = this.quizForm.value;
    if (this.isEdit && this.quizId) {
      this.quizService.update(this.quizId, quiz).subscribe(() => this.router.navigate(['/back/quizzes']));
    } else {
      this.quizService.create(quiz).subscribe(() => this.router.navigate(['/back/quizzes']));
    }
  }

  cancel(): void {
    this.router.navigate(['/back/quizzes']);
  }
}
