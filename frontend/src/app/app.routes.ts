import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

// Importation des composants (Lazy loading pour la performance)
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { 
    path: 'login', 
    loadComponent: () => import('./pages/auth/auth.component').then(m => m.AuthComponent) 
  },
  { 
    path: 'exercice', 
    loadComponent: () => import('./pages/exercice/exercice.component').then(m => m.ExerciceComponent),
    canActivate: [authGuard] // Protégé par le Guard
  },
  { 
    path: 'declaration', 
    loadComponent: () => import('./pages/declaration/declaration.component').then(m => m.DeclarationComponent),
    canActivate: [authGuard] // Protégé par le Guard
  }
];