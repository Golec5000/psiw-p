import { Component } from '@angular/core';
import { MovieService } from '../../services/movie.service';
import { MovieResponse } from '../../models/movieResponse';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-repertoire',
  standalone: true,
  templateUrl: './repertoire.component.html',
  imports: [CommonModule, FormsModule, RouterLink],
})
export class RepertoireComponent {
  selectedDate: string = new Date().toISOString().split('T')[0];
  movies: MovieResponse[] = [];

  constructor(private movieService: MovieService) {
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
    const [hour, minute] = screeningTime.split(':').map(Number);
    const date = new Date(this.selectedDate);
    date.setHours(hour, minute, 0, 0);
    return date < new Date();
  }

  formatTime(startTime: string): string {
    return startTime.slice(-5);
  }
}
