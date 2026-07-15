import { Component, inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TaxService } from '../../services/tax.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-declaration',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, 
            MatInputModule, MatButtonModule, MatIconModule, MatTableModule, MatDividerModule],
  templateUrl: './declaration.component.html',
  styleUrl: './declaration.component.scss'
})
export class DeclarationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private taxService = inject(TaxService);

  totalProduits = 0;
  totalCharges = 0;
  resultatNet = 0;
  impot = 0;
  penalite = 0;
  totalAPayer = 0;

  // Formulaire principal
  declarationForm = this.fb.group({
    produits: this.fb.array([]),
    charges: this.fb.array([])
  });

  ngOnInit() {
    // Ajouter une ligne vide par défaut
    this.addProduit();
    this.addCharge();
  }

  // Getters pour les FormArray
  get produits() { return this.declarationForm.get('produits') as FormArray; }
  get charges() { return this.declarationForm.get('charges') as FormArray; }

  addProduit() {
    this.produits.push(this.fb.group({ libelle: ['', Validators.required], montant: [0, [Validators.required, Validators.min(0)]] }));
  }
  removeProduit(index: number) { this.produits.removeAt(index); }

  addCharge() {
    this.charges.push(this.fb.group({ libelle: ['', Validators.required], montant: [0, [Validators.required, Validators.min(0)]] }));
  }
  removeCharge(index: number) { this.charges.removeAt(index); }

  // Recalcul en temps réel
  calculateTotals() {
    this.totalProduits = this.produits.controls.reduce((sum, ctrl) => sum + (ctrl.get('montant')?.value || 0), 0);
    this.totalCharges = this.charges.controls.reduce((sum, ctrl) => sum + (ctrl.get('montant')?.value || 0), 0);
    
    this.resultatNet = this.taxService.calculateNetResult(this.totalProduits, this.totalCharges);
    this.impot = this.taxService.calculateProgressiveTax(this.resultatNet);
    
    // Simulation de date de dépôt (aujourd'hui) pour la pénalité
    const dates = JSON.parse(sessionStorage.getItem('exerciceDates') || '{}');
    const dateDepot = new Date();
    this.penalite = this.taxService.calculatePenalty(this.impot, new Date(dates.dateFin), dateDepot);
    
    this.totalAPayer = this.impot + this.penalite;
  }

  submitDeclaration() {
    alert(`Déclaration enregistrée ! Impôt: ${this.impot} DH, Pénalité: ${this.penalite} DH`);
    // Ici, appel au backend Spring Boot via HttpClient
  }
}