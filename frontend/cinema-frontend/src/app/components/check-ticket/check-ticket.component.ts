import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TicketResponse } from '../../models/ticketResponse';
import { TicketDetailsComponent } from '../shared/ticket-details/ticket-details.component';
import { TicketValidationService } from '../../services/ticket-validation.service';

@Component({
  selector: 'app-check-ticket',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TicketDetailsComponent],
  templateUrl: './check-ticket.component.html',
})
export class CheckTicketComponent {
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

    this.ticketValidationService.checkTicket(ticketId).subscribe({
      next: (res) => (this.ticketResponse = res),
      error: () => (this.error = 'Nieprawidłowy identyfikator biletu'),
    });
  }
}
