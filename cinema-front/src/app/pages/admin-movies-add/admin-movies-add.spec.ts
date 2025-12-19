import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminMoviesAdd } from './admin-movies-add';

describe('AdminMoviesAdd', () => {
  let component: AdminMoviesAdd;
  let fixture: ComponentFixture<AdminMoviesAdd>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMoviesAdd]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminMoviesAdd);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
