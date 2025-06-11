import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TicketResponse } from '../../models/ticketResponse';
import { TicketValidationService } from '../../services/ticket-validation.service';
import { CommonModule } from '@angular/common';
import { TicketDetailsComponent } from '../shared/ticket-details/ticket-details.component';

@Component({
  selector: 'app-scan-ticket',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TicketDetailsComponent],
  templateUrl: './scan-ticket.component.html',
})
export class ScanTicketComponent {
  form: FormGroup;
  ticketResponse?: TicketResponse;
  error?: string;

  constructor(
    private fb: FormBuilder,
    private ticketValidationService: TicketValidationService
  ) {
    this.form = this.fb.group({
      ticketId: ['', Validators.required],
    });

    this.form.get('ticketId')?.valueChanges.subscribe(() => {
      this.ticketResponse = undefined;
      this.error = undefined;
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.ticketResponse = undefined;
    this.error = undefined;

    const ticketId = this.form.value.ticketId;

    this.ticketValidationService.scanTicket(ticketId).subscribe({
      next: (res) => (this.ticketResponse = res),
      error: (err) => {
        if (err.status === 404) {
          this.error =
            'Nie znaleziono aktywnego biletu o podanym identyfikatorze';
        } else {
          this.error = 'Nieprawidłowy identyfikator biletu';
        }
      },
    });
  }
}
