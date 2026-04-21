import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LessonsPublicComponent } from './lessons-public.component';

describe('LessonsPublicComponent', () => {
  let component: LessonsPublicComponent;
  let fixture: ComponentFixture<LessonsPublicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LessonsPublicComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LessonsPublicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
