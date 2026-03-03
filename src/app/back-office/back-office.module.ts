import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackOfficeRoutingModule } from './back-office-routing.module';
import { BackOfficeComponent } from './back-office.component';
import { SidebarComponent } from './back-layout/sidebar/sidebar.component';
import { HeaderComponent } from './back-layout/header/header.component';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';

import { QuizListComponent } from './quiz-list/quiz-list.component';
import { QuizFormComponent } from './quiz-form/quiz-form.component';
import { QuestionListComponent } from './question-list/question-list.component';
import { QuestionFormComponent } from './question-form/question-form.component';
import { ChoiceListComponent } from './choice-list/choice-list.component';
import { ChoiceFormComponent } from './choice-form/choice-form.component';
import { AttemptListComponent } from './attempt-list/attempt-list.component';
import { AttemptDetailComponent } from './attempt-detail/attempt-detail.component';
import { AiQuizGeneratorComponent } from './ai-quiz-generator/ai-quiz-generator.component';


@NgModule({
  declarations: [
    BackOfficeComponent,
    SidebarComponent,
    HeaderComponent,
    QuizListComponent,
    QuizFormComponent,
    QuestionListComponent,
    QuestionFormComponent,
    ChoiceListComponent,
    ChoiceFormComponent,
    AttemptListComponent,
    AttemptDetailComponent,
    AiQuizGeneratorComponent,
  ],
  imports: [
    CommonModule,
    BackOfficeRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    BaseChartDirective
  ]
})
export class BackOfficeModule { }
