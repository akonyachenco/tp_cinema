import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminDirectors } from './admin-directors';

describe('AdminDirectors', () => {
  let component: AdminDirectors;
  let fixture: ComponentFixture<AdminDirectors>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDirectors]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminDirectors);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
