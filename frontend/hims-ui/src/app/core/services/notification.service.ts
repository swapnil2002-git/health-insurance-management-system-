import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  constructor(private snackBar: MatSnackBar) {}

  public success(message: string, duration = 4000): void {
    this.snackBar.open(message, 'OK', {
      duration,
      panelClass: ['toast-success'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  public error(message: string, duration = 6000): void {
    this.snackBar.open(message, 'CLOSE', {
      duration,
      panelClass: ['toast-error'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  public info(message: string, duration = 3000): void {
    this.snackBar.open(message, 'DISMISS', {
      duration,
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  public warning(message: string, duration = 4000): void {
    this.snackBar.open(message, 'DISMISS', {
      duration,
      panelClass: ['toast-warning'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }
}
