import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private failAttempts = 0;
  private isBlocked = false;

  constructor(private http: HttpClient) {
    this.loadSessionState();
  }

  // Méthode sécurisée pour lire le sessionStorage
  private loadSessionState() {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      this.isBlocked = sessionStorage.getItem('isBlocked') === 'true';
      this.failAttempts = Number(sessionStorage.getItem('failAttempts')) || 0;
    }
  }

  // Méthode sécurisée pour écrire dans le sessionStorage
  private saveSessionState() {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      sessionStorage.setItem('failAttempts', this.failAttempts.toString());
      sessionStorage.setItem('isBlocked', this.isBlocked.toString());
    }
  }

  login(numFiscal: string, password: string): Observable<any> {
    if (this.isBlocked) {
      return of({ error: 'Compte bloqué suite à 3 tentatives échouées.' });
    }

    // Simulation backend (à remplacer plus tard par un vrai appel HTTP)
    if (numFiscal === '12345678' && password === 'Password123!') {
      this.failAttempts = 0;
      this.isBlocked = false;
      if (typeof window !== 'undefined' && window.sessionStorage) {
        sessionStorage.setItem('token', 'fake-jwt-token');
        sessionStorage.setItem('isBlocked', 'false');
      }
      return of({ success: true, token: 'fake-jwt-token' });
    } else {
      this.failAttempts++;
      this.saveSessionState();
      
      if (this.failAttempts >= 3) {
        this.isBlocked = true;
        this.saveSessionState();
        return of({ error: 'Compte bloqué définitivement.' });
      }
      return of({ error: `Échec. Il vous reste ${3 - this.failAttempts} tentatives.` });
    }
  }

  isAuthenticated(): boolean {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      return !!sessionStorage.getItem('token');
    }
    return false;
  }

  logout() {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      sessionStorage.clear();
    }
    this.failAttempts = 0;
    this.isBlocked = false;
  }
}