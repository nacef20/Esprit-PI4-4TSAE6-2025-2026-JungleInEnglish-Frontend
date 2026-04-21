import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScrollAnimationComponent } from './scroll-animation.component';

describe('ScrollAnimationComponent', () => {
  let component: ScrollAnimationComponent;
  let fixture: ComponentFixture<ScrollAnimationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScrollAnimationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ScrollAnimationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
