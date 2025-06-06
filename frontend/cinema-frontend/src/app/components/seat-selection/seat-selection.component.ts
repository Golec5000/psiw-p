import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ScreeningService } from '../../services/screening.service';
import { ScreeningDetailsResponse } from '../../models/screeningDetailsResponse';
import { SeatDto } from '../../models/seatDto';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ReservationDialogComponent } from '../reservation-dialog/reservation-dialog.component';

@Component({
  selector: 'app-seat-selection',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './seat-selection.component.html',
  styleUrls: ['./seat-selection.component.css'],
})
export class SeatSelectionComponent implements OnInit {
  screeningDetails: ScreeningDetailsResponse | null = null;
  selectedSeats: SeatDto[] = [];

  constructor(
    private route: ActivatedRoute,
    private screeningService: ScreeningService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.screeningService.getDetails(id).subscribe({
      next: (data) => (this.screeningDetails = data),
      error: (err) => console.error('Błąd ładowania szczegółów seansu', err),
    });
  }

  toggleSeat(seat: SeatDto): void {
    if (!seat.available) return;
    const index = this.selectedSeats.findIndex((s) => s.id === seat.id);
    if (index >= 0) {
      this.selectedSeats.splice(index, 1);
    } else {
      this.selectedSeats.push(seat);
    }
  }

  isSelected(seat: SeatDto): boolean {
    return this.selectedSeats.some((s) => s.id === seat.id);
  }

  getSeatsForRow(row: number): SeatDto[] {
    return (
      this.screeningDetails?.seats.filter((s) => s.rowNumber === row) || []
    );
  }

  openReservationDialog(): void {
    if (!this.screeningDetails) return;

    this.dialog.open(ReservationDialogComponent, {
      data: {
        screeningId: this.screeningDetails.id,
        selectedSeats: this.selectedSeats,
        movieTitle: this.screeningDetails.movie.title,
        screeningStartTime: this.screeningDetails.startTime,
      },
      width: '600px',
    });
  }
}
