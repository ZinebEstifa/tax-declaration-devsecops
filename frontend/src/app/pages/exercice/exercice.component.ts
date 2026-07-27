import { Component, inject, OnInit } from '@angular/core';
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
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../services/auth.service';
import { TaxService } from '../../services/tax.service';

@Component({
  selector: 'app-exercice',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatDatepickerModule, MatNativeDateModule, 
            MatFormFieldModule, MatButtonModule, MatInputModule, MatIconModule, MatChipsModule],
  templateUrl: './exercice.component.html',
  styleUrl: './exercice.component.scss'
})
export class ExerciceComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);
  private taxService = inject(TaxService);

  declarations: any[] = [];
  isLoading = false;
  numeroFiscal = '';

  exerciceForm = this.fb.group({
    dateDebut: [null, [Validators.required]],
    dateFin: [null, [Validators.required, this.exerciceDurationValidator]]
  });

  ngOnInit() {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      this.numeroFiscal = sessionStorage.getItem('numeroFiscal') || '12345678';
    }
    this.loadDeclarations();
  }

  loadDeclarations() {
    this.isLoading = true;
    this.taxService.getDeclarations().subscribe({
      next: (data) => {
        this.declarations = data || [];
        this.isLoading = false;
      },
      error: (err) => {
        console.error("Erreur lors de la récupération des déclarations", err);
        this.isLoading = false;
        if (err.status === 401 || err.status === 403) {
          alert("Votre session a expiré ou nécessite une réauthentification. Veuillez vous reconnecter.");
          this.authService.logout();
          this.router.navigate(['/login']);
        }
      }
    });
  }

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
      sessionStorage.setItem('exerciceDates', JSON.stringify(this.exerciceForm.value));
      this.router.navigate(['/declaration']);
    }
  }

  viewDeclaration(id: number) {
    this.router.navigate(['/declaration'], { queryParams: { id: id } });
  }

  editDeclaration(id: number) {
    this.router.navigate(['/declaration'], { queryParams: { id: id } });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}