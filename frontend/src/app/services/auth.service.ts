import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

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

    return this.http.post<any>(`${this.apiUrl}/login`, { numFiscal, password }).pipe(
      tap((res) => {
        if (res.success && res.token) {
          this.failAttempts = 0;
          this.isBlocked = false;
          if (typeof window !== 'undefined' && window.sessionStorage) {
            sessionStorage.setItem('token', res.token);
            sessionStorage.setItem('numeroFiscal', res.numeroFiscal || numFiscal);
            sessionStorage.setItem('failAttempts', '0');
            sessionStorage.setItem('isBlocked', 'false');
          }
        }
      }),
      catchError((err) => {
        const errorMsg = err.error?.error || 'Échec de la connexion.';
        if (err.status === 403) {
          this.isBlocked = true;
          this.failAttempts = 3;
          this.saveSessionState();
        } else if (err.status === 401) {
          this.failAttempts++;
          this.saveSessionState();
          if (this.failAttempts >= 3) {
            this.isBlocked = true;
            this.saveSessionState();
          }
        }
        return of({ success: false, error: errorMsg });
      })
    );
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