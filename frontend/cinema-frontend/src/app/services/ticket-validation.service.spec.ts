import { TestBed } from '@angular/core/testing';

import { TicketValidationService } from './ticket-validation.service';

describe('TicketValidationService', () => {
  let service: TicketValidationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TicketValidationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
