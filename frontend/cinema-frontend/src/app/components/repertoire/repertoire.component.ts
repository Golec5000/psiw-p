import { Component } from '@angular/core';
import { MovieService } from '../../services/movie.service';
import { MovieResponse } from '../../models/movieResponse';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ScreeningSummaryDto } from '../../models/screeningSummaryDto';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MovieDetailsDialogComponent } from '../shared/movie-details-dialog/movie-details-dialog.component';

@Component({
  selector: 'app-repertoire',
  standalone: true,
  templateUrl: './repertoire.component.html',
  imports: [CommonModule, FormsModule, RouterLink, MatDialogModule],
})
export class RepertoireComponent {
  selectedDate: string = new Date().toISOString().split('T')[0];
  movies: MovieResponse[] = [];
  readonly hours = Array.from({ length: 13 }, (_, i) => i + 10);

  constructor(private movieService: MovieService, private dialog: MatDialog) {
    this.fetchMovies();
  }

  onDateChange(): void {
    this.fetchMovies();
  }

  fetchMovies(): void {
    this.movieService.getMoviesByDate(this.selectedDate).subscribe({
      next: (data) => (this.movies = data),
      error: (err) => console.error(err),
    });
  }

  isPastScreening(screeningTime: string): boolean {
    const screeningDate = new Date(screeningTime);
    return screeningDate.getTime() < new Date().getTime();
  }

  formatTime(dateStr: string): string {
    return new Date(dateStr).toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  groupScreeningsByHour(
    screenings: ScreeningSummaryDto[]
  ): Record<number, ScreeningSummaryDto[]> {
    const grouped: Record<number, ScreeningSummaryDto[]> = {};

    for (const screening of screenings) {
      const hour = new Date(screening.startTime).getHours();
      if (!grouped[hour]) {
        grouped[hour] = [];
      }
      grouped[hour].push(screening);
    }

    return grouped;
  }

  openMovieDetails(movie: MovieResponse): void {
    this.dialog.open(MovieDetailsDialogComponent, {
      data: movie,
      width: '500px',
    });
  }

  getDurationWidthStyle(durationInMinutes: number): { [key: string]: string } {
    const pixelsPerMinute = 1.5;
    const width = durationInMinutes * pixelsPerMinute;
    return {
      width: `${width}px`,
    };
  }
}
