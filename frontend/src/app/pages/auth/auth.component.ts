import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, 
            MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.scss'
})
export class AuthComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  errorMessage = '';
  hidePassword = true;

  // Création du formulaire réactif
  loginForm = this.fb.group({
    numFiscal: ['', [Validators.required, Validators.pattern('^[0-9]{8,}$')]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  onSubmit() {
    if (this.loginForm.valid) {
      const { numFiscal, password } = this.loginForm.value;
      this.authService.login(numFiscal!, password!).subscribe({
        next: (res) => {
          if (res.success) {
            this.router.navigate(['/exercice']); // Succès -> Page 2
          } else {
            this.errorMessage = res.error;
          }
        }
      });
    }
  }
}