import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ScreeningDetailsResponse } from '../models/screeningDetailsResponse';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ScreeningService {
  private apiUrl = 'http://localhost:8081/psiw/api/v1/open/repertoire';

  constructor(private http: HttpClient) {}

  getDetails(screeningId: number): Observable<ScreeningDetailsResponse> {
    return this.http.get<ScreeningDetailsResponse>(
      `${this.apiUrl}/movie-screening?screeningId=${screeningId}`
    );
  }
}
