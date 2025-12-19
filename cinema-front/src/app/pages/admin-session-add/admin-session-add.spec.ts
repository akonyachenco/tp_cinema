import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSessionAdd } from './admin-session-add';

describe('AdminSessionAdd', () => {
  let component: AdminSessionAdd;
  let fixture: ComponentFixture<AdminSessionAdd>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSessionAdd]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminSessionAdd);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
