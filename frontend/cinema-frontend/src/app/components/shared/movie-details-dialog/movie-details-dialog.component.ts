import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MovieResponse } from '../../../models/movieResponse';

@Component({
  selector: 'app-movie-details-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './movie-details-dialog.component.html',
})
export class MovieDetailsDialogComponent {
  imageUrl: string;

  constructor(@Inject(MAT_DIALOG_DATA) public data: MovieResponse) {
    this.imageUrl = `http://localhost:8081/psiw/api/v1/open/movies/${data.id}/image`;
  }
}
