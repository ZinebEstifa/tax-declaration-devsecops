import { Component, inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { TaxService } from '../../services/tax.service';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-declaration',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, 
            MatInputModule, MatButtonModule, MatIconModule, MatTableModule, MatDividerModule, MatChipsModule],
  templateUrl: './declaration.component.html',
  styleUrl: './declaration.component.scss'
})
export class DeclarationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private taxService = inject(TaxService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  totalProduits = 0;
  totalCharges = 0;
  resultatNet = 0;
  impot = 0;
  penalite = 0;
  totalAPayer = 0;

  numeroFiscal = '';
  declarationId: number | null = null;
  statutDeclaration: string = 'NOUVELLE';
  dateDepot: string | null = null;
  dateDebutExercice: string | null = null;
  dateFinExercice: string | null = null;
  isReadOnly = false;

  // Formulaire principal
  declarationForm = this.fb.group({
    produits: this.fb.array([]),
    charges: this.fb.array([])
  });

  ngOnInit() {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      this.numeroFiscal = sessionStorage.getItem('numeroFiscal') || '12345678';
    }

    // Récupérer l'ID depuis l'URL si présent
    this.route.queryParams.subscribe(params => {
      const id = params['id'];
      const mode = params['mode'];
      if (id) {
        this.declarationId = +id;
        this.loadExistingDeclaration(this.declarationId, mode === 'view');
      } else {
        // Nouvelle déclaration par défaut
        const dates = JSON.parse(sessionStorage.getItem('exerciceDates') || '{}');
        this.dateDebutExercice = dates.dateDebut || null;
        this.dateFinExercice = dates.dateFin || null;
        this.addProduit();
        this.addCharge();
      }
    });
  }

  loadExistingDeclaration(id: number, forceReadOnly: boolean = false) {
    this.taxService.getDeclarationById(id).subscribe({
      next: (dec) => {
        this.statutDeclaration = dec.statut;
        this.dateDepot = dec.dateDepot;
        this.dateDebutExercice = dec.dateDebutExercice;
        this.dateFinExercice = dec.dateFinExercice;
        this.totalProduits = dec.totalProduits || 0;
        this.totalCharges = dec.totalCharges || 0;
        this.resultatNet = dec.resultatNet || 0;
        this.impot = dec.montantImpot || 0;
        this.penalite = dec.penaliteRetard || 0;
        this.totalAPayer = this.impot + this.penalite;

        // Vider les FormArray
        this.produits.clear();
        this.charges.clear();

        // Remplir les produits
        if (dec.produits && dec.produits.length > 0) {
          dec.produits.forEach((p: any) => {
            this.produits.push(this.fb.group({
              id: [p.id],
              libelle: [p.libelle, Validators.required],
              montant: [p.montant, [Validators.required, Validators.min(0)]]
            }));
          });
        } else {
          this.addProduit();
        }

        // Remplir les charges
        if (dec.charges && dec.charges.length > 0) {
          dec.charges.forEach((c: any) => {
            this.charges.push(this.fb.group({
              id: [c.id],
              libelle: [c.libelle, Validators.required],
              montant: [c.montant, [Validators.required, Validators.min(0)]]
            }));
          });
        } else {
          this.addCharge();
        }

        if (forceReadOnly) {
          this.enableReadOnlyMode();
        } else {
          this.enableEditMode();
        }
      },
      error: (err) => {
        alert("Impossible de charger la déclaration: " + (err.error?.error || err.message));
        this.router.navigate(['/exercice']);
      }
    });
  }

  enableEditMode() {
    this.isReadOnly = false;
    this.declarationForm.enable();
  }

  enableReadOnlyMode() {
    this.isReadOnly = true;
    this.declarationForm.disable();
  }

  // Getters pour les FormArray
  get produits() { return this.declarationForm.get('produits') as FormArray; }
  get charges() { return this.declarationForm.get('charges') as FormArray; }

  addProduit() {
    if (this.isReadOnly) return;
    this.produits.push(this.fb.group({ libelle: ['', Validators.required], montant: [0, [Validators.required, Validators.min(0)]] }));
  }
  removeProduit(index: number) {
    if (this.isReadOnly) return;
    this.produits.removeAt(index);
  }

  addCharge() {
    if (this.isReadOnly) return;
    this.charges.push(this.fb.group({ libelle: ['', Validators.required], montant: [0, [Validators.required, Validators.min(0)]] }));
  }
  removeCharge(index: number) {
    if (this.isReadOnly) return;
    this.charges.removeAt(index);
  }

  // Recalcul en temps réel
  calculateTotals() {
    if (this.isReadOnly) return;
    this.totalProduits = this.produits.controls.reduce((sum, ctrl) => sum + (ctrl.get('montant')?.value || 0), 0);
    this.totalCharges = this.charges.controls.reduce((sum, ctrl) => sum + (ctrl.get('montant')?.value || 0), 0);
    
    this.resultatNet = this.taxService.calculateNetResult(this.totalProduits, this.totalCharges);
    this.impot = this.taxService.calculateProgressiveTax(this.resultatNet);
    
    const dateFin = this.dateFinExercice ? new Date(this.dateFinExercice) : new Date();
    const dateDepot = new Date();
    this.penalite = this.taxService.calculatePenalty(this.impot, dateFin, dateDepot);
    
    this.totalAPayer = this.impot + this.penalite;
  }

  // Action 1: Enregistrer le Brouillon
  saveDraft() {
    const payload = this.getPayload();
    if (!payload) return;

    if (this.declarationId) {
      this.taxService.updateDraft(this.declarationId, payload).subscribe({
        next: (res) => {
          alert("Brouillon mis à jour avec succès.");
          this.statutDeclaration = res.statut;
        },
        error: (err) => alert("Erreur: " + (err.error?.error || err.message))
      });
    } else {
      this.taxService.createDraft(payload).subscribe({
        next: (res) => {
          this.declarationId = res.id;
          this.statutDeclaration = res.statut;
          alert("Brouillon créé avec succès (ID: " + res.id + ").");
        },
        error: (err) => alert("Erreur: " + (err.error?.error || err.message))
      });
    }
  }

  // Action 2: Valider la déclaration (Statut -> VALIDEE)
  validateDeclaration() {
    const payload = this.getPayload();
    if (!payload) return;

    if (this.declarationId) {
      this.taxService.validateDeclaration(this.declarationId, payload).subscribe({
        next: (res) => {
          this.statutDeclaration = res.statut;
          alert("Déclaration contrôlée et VALIDÉE avec succès (ID: #" + res.id + ") !");
        },
        error: (err) => alert("Erreur: " + (err.error?.error || err.message))
      });
    } else {
      this.taxService.createDraft(payload).subscribe({
        next: (draftRes) => {
          this.declarationId = draftRes.id;
          this.taxService.validateDeclaration(draftRes.id, payload).subscribe({
            next: (res) => {
              this.statutDeclaration = res.statut;
              alert("Déclaration contrôlée et VALIDÉE avec succès (ID: #" + res.id + ") !");
            },
            error: (err) => alert("Erreur: " + (err.error?.error || err.message))
          });
        },
        error: (err) => alert("Erreur: " + (err.error?.error || err.message))
      });
    }
  }

  // Action 3: Déposer Officiellement (Statut -> DEPOSEE)
  submitDeclaration() {
    const payload = this.getPayload();
    if (!payload) return;

    const proceedDeposit = (id: number) => {
      this.taxService.deposit(id).subscribe({
        next: (depositRes) => {
          alert(`Déclaration #${depositRes.id} DÉPOSÉE officiellement avec succès !\nImpôt final: ${depositRes.montantImpot} DH\nPénalités: ${depositRes.penaliteRetard} DH\nTotal réglé: ${depositRes.montantImpot + depositRes.penaliteRetard} DH`);
          sessionStorage.removeItem('exerciceDates');
          this.router.navigate(['/exercice']);
        },
        error: (err) => alert("Erreur lors du dépôt officiel: " + (err.error?.error || err.message))
      });
    };

    if (this.declarationId) {
      this.taxService.updateDraft(this.declarationId, payload).subscribe({
        next: (res) => proceedDeposit(res.id),
        error: (err) => alert("Erreur: " + (err.error?.error || err.message))
      });
    } else {
      this.taxService.createDraft(payload).subscribe({
        next: (res) => proceedDeposit(res.id),
        error: (err) => alert("Erreur: " + (err.error?.error || err.message))
      });
    }
  }

  // Action 4: Rectifier une déclaration déposée (Statut -> RECTIFIEE)
  rectifyDeclaration() {
    const payload = this.getPayload();
    if (!payload || !this.declarationId) return;

    this.taxService.rectifyDeclaration(this.declarationId, payload).subscribe({
      next: (rectRes) => {
        alert(`Déclaration #${rectRes.id} RECTIFIÉE et transmise avec succès !\nNouveau Résultat Net: ${rectRes.resultatNet} DH\nImpôt ajusté: ${rectRes.montantImpot} DH`);
        this.router.navigate(['/exercice']);
      },
      error: (err) => alert("Erreur lors de la rectification: " + (err.error?.error || err.message))
    });
  }

  private getPayload() {
    let dateDebut = this.dateDebutExercice;
    let dateFin = this.dateFinExercice;

    if (!dateDebut || !dateFin) {
      const dates = JSON.parse(sessionStorage.getItem('exerciceDates') || '{}');
      dateDebut = dates.dateDebut;
      dateFin = dates.dateFin;
    }

    if (!dateDebut || !dateFin) {
      alert("Erreur: Les dates d'exercice fiscal sont manquantes.");
      return null;
    }

    return {
      dateDebutExercice: dateDebut,
      dateFinExercice: dateFin,
      produits: this.produits.value,
      charges: this.charges.value
    };
  }

  goBack() {
    this.router.navigate(['/exercice']);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}