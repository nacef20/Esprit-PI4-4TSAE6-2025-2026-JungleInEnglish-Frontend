import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionchatpriveComponent } from './gestionchatprive.component';

describe('GestionchatpriveComponent', () => {
  let component: GestionchatpriveComponent;
  let fixture: ComponentFixture<GestionchatpriveComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GestionchatpriveComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionchatpriveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
