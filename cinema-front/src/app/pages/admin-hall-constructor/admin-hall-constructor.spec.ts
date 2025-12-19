import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHallConstructor } from './admin-hall-constructor';

describe('AdminHallConstructor', () => {
  let component: AdminHallConstructor;
  let fixture: ComponentFixture<AdminHallConstructor>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHallConstructor]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminHallConstructor);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
