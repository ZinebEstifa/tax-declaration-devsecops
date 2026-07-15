import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TaxService {
  
  // Règle métier 1 : Calcul du Résultat Net
  calculateNetResult(products: number, charges: number): number {
    return products - charges;
  }

  // Règle métier 2 : Barème progressif par tranches marginales
  calculateProgressiveTax(netResult: number): number {
    if (netResult <= 0) return 0;

    let tax = 0;
    // Tranche 1 : 0 à 100 000 DH -> 10%
    if (netResult <= 100000) {
      tax = netResult * 0.10;
    } 
    // Tranche 2 : 100 001 à 1 000 000 DH -> 20%
    else if (netResult <= 1000000) {
      tax = (100000 * 0.10) + ((netResult - 100000) * 0.20);
    } 
    // Tranche 3 : > 1 000 000 DH -> 30%
    else {
      tax = (100000 * 0.10) + (900000 * 0.20) + ((netResult - 1000000) * 0.30);
    }
    return tax;
  }

  // Règle métier 3 : Pénalité de retard (10% si dépôt > fin exercice + 3 mois)
  calculatePenalty(taxAmount: number, dateFinExercice: Date, dateDepot: Date): number {
    const dateLimite = new Date(dateFinExercice);
    dateLimite.setMonth(dateLimite.getMonth() + 3); // + 3 mois

    if (dateDepot > dateLimite) {
      return taxAmount * 0.10; // Pénalité de 10%
    }
    return 0;
  }
}