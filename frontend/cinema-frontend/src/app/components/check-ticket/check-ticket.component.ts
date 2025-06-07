import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-check-ticket',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './check-ticket.component.html',
})
export class CheckTicketComponent {
  form: FormGroup;
  status?: string;
  error?: string;

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.form = this.fb.group({
      ticketId: ['', Validators.required],
    });

    this.form.get('ticketId')?.valueChanges.subscribe(() => {
      this.status = undefined;
      this.error = undefined;
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.status = undefined;
    this.error = undefined;

    const ticketId = this.form.value.ticketId;
    this.http
      .get<string>(
        `http://localhost:8081/psiw/api/v1/auth/ticket-validation/check-staus`,
        {
          params: { ticketId },
        }
      )
      .subscribe({
        next: (res) => (this.status = res),
        error: () => (this.error = 'Nieprawidłowy identyfikator biletu'),
      });
  }
}
