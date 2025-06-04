import { Component } from '@angular/core';
import { AuthService } from '../../../auth/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { LoginDialogComponent } from '../../../auth/login-dialog/login-dialog.component';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './main-layout.component.html',
})
export class MainLayoutComponent {
  currentYear = new Date().getFullYear();

  constructor(public auth: AuthService, private dialog: MatDialog) {}

  openLogin() {
    this.dialog.open(LoginDialogComponent, { width: '400px' });
  }

  logout() {
    this.auth.logout();
  }
}
