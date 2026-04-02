import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { BackOfficeRoutingModule } from './back-office-routing.module';
import { BackOfficeComponent } from './back-office.component';
import { SidebarComponent } from './back-layout/sidebar/sidebar.component';
import { HeaderComponent } from './back-layout/header/header.component';

import { QuizListComponent } from './quiz-list/quiz-list.component';
import { QuizFormComponent } from './quiz-form/quiz-form.component';
import { AttemptListComponent } from './attempt-list/attempt-list.component';
import { AttemptDetailComponent } from './attempt-detail/attempt-detail.component';
import { AiQuizGeneratorComponent } from './ai-quiz-generator/ai-quiz-generator.component';
import { QuestionListComponent } from './question-list/question-list.component';
import { QuestionFormComponent } from './question-form/question-form.component';
import { ChoiceListComponent } from './choice-list/choice-list.component';
import { ChoiceFormComponent } from './choice-form/choice-form.component';

import { QuizStudentListComponent } from '../front-office/quiz-student-list/quiz-student-list.component';
import { QuizPassComponent } from '../front-office/quiz-pass/quiz-pass.component';
import { AttemptHistoryComponent } from '../front-office/attempt-history/attempt-history.component';
import { AttemptDetailStudentComponent } from '../front-office/attempt-detail/attempt-detail.component';

import { NgChartsModule } from 'ng2-charts';
import { SalleModule } from './salle/salle.module';

import { CoursesManagementComponent } from './pages/courses-management/courses-management.component';
import { LessonsManagementComponent } from './pages/lessons-management/lessons-management.component';
import { LessonsListComponent } from '../components/lessons-list/lessons-list.component';
import { MyCoursesComponent } from './pages/my-courses/my-courses.component';

import { ComplaintListComponent } from './complaint-list/complaint-list.component';
import { ComplaintFormComponent } from './complaint-form/complaint-form.component';
import { ComplaintDetailComponent } from './complaint-detail/complaint-detail.component';

@NgModule({
  declarations: [
    BackOfficeComponent,
    SidebarComponent,
    HeaderComponent,
    CoursesManagementComponent,
    LessonsManagementComponent,
    LessonsListComponent,
    MyCoursesComponent,
    QuizListComponent,
    QuizFormComponent,
    AttemptListComponent,
    AttemptDetailComponent,
    AiQuizGeneratorComponent,
    QuestionListComponent,
    QuestionFormComponent,
    ChoiceListComponent,
    ChoiceFormComponent,
    QuizStudentListComponent,
    QuizPassComponent,
    AttemptHistoryComponent,
    AttemptDetailStudentComponent,
    ComplaintListComponent,
    ComplaintFormComponent,
    ComplaintDetailComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BackOfficeRoutingModule,
    NgChartsModule,
    SalleModule
  ]
})
export class BackOfficeModule { }