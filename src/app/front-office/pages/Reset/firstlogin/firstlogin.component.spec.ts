import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FirstloginComponent } from './firstlogin.component';

describe('FirstloginComponent', () => {
  let component: FirstloginComponent;
  let fixture: ComponentFixture<FirstloginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FirstloginComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FirstloginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
