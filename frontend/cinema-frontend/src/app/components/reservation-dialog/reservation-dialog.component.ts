import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { Router } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { SeatDto } from '../../models/seatDto';
import { TicketResponse } from '../../models/ticketResponse';
import { TicketDetailsComponent } from '../shared/ticket-details/ticket-details.component';

@Component({
  selector: 'app-reservation-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    ReactiveFormsModule,
    TicketDetailsComponent,
  ],
  templateUrl: './reservation-dialog.component.html',
})
export class ReservationDialogComponent {
  form: FormGroup;
  ticketResponse?: TicketResponse;
  loading = false;
  error?: string;

  constructor(
    private fb: FormBuilder,
    private reservationService: ReservationService,
    private dialogRef: MatDialogRef<ReservationDialogComponent>,
    private router: Router,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      screeningId: number;
      selectedSeats: SeatDto[];
      movieTitle: string;
      screeningStartTime: string;
    }
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.reservationService
      .confirmReservation({
        screeningId: this.data.screeningId,
        seatIds: this.data.selectedSeats.map((s) => s.id),
        ...this.form.value,
      })
      .subscribe({
        next: (res) => {
          this.ticketResponse = res;
          this.loading = false;
          this.dialogRef.disableClose = true;
        },
        error: (_err) => {
          this.error = 'Błąd rezerwacji. Spróbuj ponownie.';
          this.loading = false;
        },
      });
  }

  close(): void {
    this.dialogRef.close();
    this.router.navigate(['/repertoire']);
  }
}
