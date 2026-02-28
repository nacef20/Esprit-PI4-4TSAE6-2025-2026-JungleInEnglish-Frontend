import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Choice } from '../../models/choice.model';
import { ChoiceService } from '../../services/choice.service';

@Component({
  selector: 'app-choice-list',
  templateUrl: './choice-list.component.html',
  styleUrls: ['./choice-list.component.scss']
})
export class ChoiceListComponent implements OnInit {

  choices: Choice[] = [];
  questionId!: number;

  constructor(
    private choiceService: ChoiceService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.questionId = +this.route.snapshot.params['questionId'];
    this.loadChoices();
  }

  loadChoices(): void {
    this.choiceService.getByQuestionId(this.questionId).subscribe(data => {
      this.choices = data;
    });
  }

  addChoice(): void {
    this.router.navigate(['/back/questions', this.questionId, 'choices', 'new']);
  }

  editChoice(choiceId: number): void {
    this.router.navigate(['/back/questions', this.questionId, 'choices', 'edit', choiceId]);
  }

  deleteChoice(id: number): void {
    if (confirm('Delete this choice?')) {
      this.choiceService.delete(id).subscribe(() => this.loadChoices());
    }
  }

  goBack(): void {
    window.history.back();
  }
}
