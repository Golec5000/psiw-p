import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TicketResponse } from '../models/ticketResponse';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TicketValidationService {
  private baseUrl = 'http://localhost:8081/psiw/api/v1/auth/ticket-validation';

  constructor(private http: HttpClient) {}

  checkTicket(ticketId: string): Observable<TicketResponse> {
    return this.http.get<TicketResponse>(`${this.baseUrl}/check-status`, {
      params: { ticketId },
    });
  }
}
