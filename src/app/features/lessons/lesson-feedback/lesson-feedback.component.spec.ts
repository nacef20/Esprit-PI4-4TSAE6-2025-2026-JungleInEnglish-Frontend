import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LessonFeedbackComponent } from './lesson-feedback.component';


describe('LessonFeedbackComponent', () => {
  let component: LessonFeedbackComponent;
  let fixture: ComponentFixture<LessonFeedbackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LessonFeedbackComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LessonFeedbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
