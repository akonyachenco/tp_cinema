import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHalls } from './admin-halls';

describe('AdminHalls', () => {
  let component: AdminHalls;
  let fixture: ComponentFixture<AdminHalls>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHalls]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminHalls);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
