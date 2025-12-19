import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSession } from './admin-session';

describe('AdminSession', () => {
  let component: AdminSession;
  let fixture: ComponentFixture<AdminSession>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSession]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminSession);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
