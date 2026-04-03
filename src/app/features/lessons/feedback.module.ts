import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LessonFeedbackComponent } from './lesson-feedback/lesson-feedback.component';

@NgModule({
  declarations: [LessonFeedbackComponent],
  imports: [CommonModule, FormsModule],
  exports: [LessonFeedbackComponent]   // ← both AppModule and BackOfficeModule import this
})
export class FeedbackModule {}