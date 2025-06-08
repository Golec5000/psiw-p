import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketResponse } from '../../../models/ticketResponse';

@Component({
  selector: 'app-ticket-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ticket-details.component.html',
})
export class TicketDetailsComponent {
  @Input() ticket!: TicketResponse;

  getTicketStatusDisplayText(): string {
    if (this.ticket.status === TicketResponse.StatusEnum.WaitingForActivation)
      return 'OCZEKUJE NA AKTYWACJĘ';
    if (this.ticket.status === TicketResponse.StatusEnum.Valid)
      return 'AKTYWNY';
    if (this.ticket.status === TicketResponse.StatusEnum.Used)
      return 'SKASOWANY';
    if (this.ticket.status === TicketResponse.StatusEnum.Expired)
      return 'NIEWAŻNY';
    return 'NIEZNANY';
  }
}
