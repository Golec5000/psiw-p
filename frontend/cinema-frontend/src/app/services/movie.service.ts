// movie.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MovieResponse } from '../models/movieResponse';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MovieService {
  private apiUrl = 'http://localhost:8081/psiw/api/v1/open/repertoire/movies';

  constructor(private http: HttpClient) {}

  getMoviesByDate(date: string): Observable<MovieResponse[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<MovieResponse[]>(this.apiUrl, { params });
  }
}
