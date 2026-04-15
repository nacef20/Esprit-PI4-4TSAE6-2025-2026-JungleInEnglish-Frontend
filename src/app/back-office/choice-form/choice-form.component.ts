import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ChoiceService } from '../../services/choice.service';

@Component({
  selector: 'app-choice-form',
  templateUrl: './choice-form.component.html',
  styleUrls: ['./choice-form.component.scss']
})
export class ChoiceFormComponent implements OnInit {

  choiceForm!: FormGroup;
  isEdit = false;
  questionId!: number;
  choiceId?: number;
  serverErrors: { [key: string]: string } = {};
  submitting = false;
  generalError = '';

  constructor(
    private fb: FormBuilder,
    private choiceService: ChoiceService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.questionId = +this.route.snapshot.params['questionId'];
    this.choiceId = +this.route.snapshot.params['choiceId'] || undefined;

    this.choiceForm = this.fb.group({
      content: [''],
      isCorrect: [false]
    });

    if (this.choiceId) {
      this.isEdit = true;
      this.choiceService.getById(this.choiceId).subscribe(choice => {
        this.choiceForm.patchValue(choice);
      });
    }
  }

  onSubmit(): void {
    this.generalError = '';
    this.serverErrors = {};
    this.submitting = true;

    const choice = {
      ...this.choiceForm.value,
      question: { id: this.questionId }
    };

    const request = this.isEdit && this.choiceId
      ? this.choiceService.update(this.choiceId, choice)
      : this.choiceService.create(choice);

    request.subscribe({
      next: () => this.router.navigate(['/back/questions', this.questionId, 'choices']),
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
    this.router.navigate(['/back/questions', this.questionId, 'choices']);
  }
}
