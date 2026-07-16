import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, ValidationErrors, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-exercice',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatDatepickerModule, MatNativeDateModule, 
            MatFormFieldModule, MatButtonModule, MatInputModule, MatIconModule],
  templateUrl: './exercice.component.html',
  styleUrl: './exercice.component.scss'
})
export class ExerciceComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  exerciceForm = this.fb.group({
    dateDebut: [null, [Validators.required]],
    dateFin: [null, [Validators.required, this.exerciceDurationValidator]]
  });

  // Validateur personnalisé : Règle métier "Durée <= 1 an (365 jours)"
  exerciceDurationValidator(control: AbstractControl): ValidationErrors | null {
    const formGroup = control.parent;
    if (formGroup) {
      const debut = formGroup.get('dateDebut')?.value;
      const fin = control.value;
      if (debut && fin) {
        const diffTime = Math.abs(fin - debut);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        if (diffDays > 365) {
          return { dureeInvalid: true }; // Retourne une erreur si > 365 jours
        }
      }
    }
    return null;
  }

  next() {
    if (this.exerciceForm.valid) {
      // Sauvegarde temporaire des dates pour la page 3
      sessionStorage.setItem('exerciceDates', JSON.stringify(this.exerciceForm.value));
      this.router.navigate(['/declaration']);
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}