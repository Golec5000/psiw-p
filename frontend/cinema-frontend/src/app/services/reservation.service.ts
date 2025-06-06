import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ReservationRequest } from '../models/reservationRequest';
import { TicketResponse } from '../models/ticketResponse';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private baseUrl = 'http://localhost:8081/psiw/api/v1/open/reservations';

  constructor(private http: HttpClient) {}

  confirmReservation(request: ReservationRequest): Observable<TicketResponse> {
    return this.http.post<TicketResponse>(`${this.baseUrl}/confirm`, request);
  }
}
