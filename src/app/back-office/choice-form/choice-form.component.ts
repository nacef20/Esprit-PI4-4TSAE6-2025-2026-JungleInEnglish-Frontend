import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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
      content: ['', Validators.required],
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
    if (this.choiceForm.invalid) return;

    const choice = {
      ...this.choiceForm.value,
      question: { id: this.questionId }
    };

    if (this.isEdit && this.choiceId) {
      this.choiceService.update(this.choiceId, choice).subscribe(() =>
        this.router.navigate(['/back/questions', this.questionId, 'choices'])
      );
    } else {
      this.choiceService.create(choice).subscribe(() =>
        this.router.navigate(['/back/questions', this.questionId, 'choices'])
      );
    }
  }

  cancel(): void {
    this.router.navigate(['/back/questions', this.questionId, 'choices']);
  }
}
