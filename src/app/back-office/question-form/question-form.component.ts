import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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
      content: ['', Validators.required],
      points: [1, Validators.required]
    });

    if (this.questionId) {
      this.isEdit = true;
      this.questionService.getById(this.questionId).subscribe(question => {
        this.questionForm.patchValue(question);
      });
    }
  }

  onSubmit(): void {
    if (this.questionForm.invalid) return;

    const question = {
      ...this.questionForm.value,
      quiz: { id: this.quizId }
    };

    if (this.isEdit && this.questionId) {
      this.questionService.update(this.questionId, question).subscribe(() =>
        this.router.navigate(['/back/quizzes', this.quizId, 'questions'])
      );
    } else {
      this.questionService.create(question).subscribe(() =>
        this.router.navigate(['/back/quizzes', this.quizId, 'questions'])
      );
    }
  }

  cancel(): void {
    this.router.navigate(['/back/quizzes', this.quizId, 'questions']);
  }
}
