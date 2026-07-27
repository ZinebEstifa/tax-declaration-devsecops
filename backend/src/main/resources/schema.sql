-- Script DDL de la Base de Données (Déclarations Fiscales)

CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    numero_fiscal VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    raison_sociale VARCHAR(255),
    tentatives_echouees INT DEFAULT 0,
    compte_bloque BOOLEAN DEFAULT FALSE,
    derniere_connexion TIMESTAMP
);

CREATE TABLE IF NOT EXISTS declarations (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    date_debut_exercice DATE NOT NULL,
    date_fin_exercice DATE NOT NULL,
    date_depot DATE,
    total_produits NUMERIC(15, 2),
    total_charges NUMERIC(15, 2),
    resultat_net NUMERIC(15, 2),
    montant_impot NUMERIC(15, 2),
    penalite_retard NUMERIC(15, 2),
    statut VARCHAR(30) NOT NULL DEFAULT 'BROUILLON',
    date_creation TIMESTAMP,
    date_modification TIMESTAMP
);

CREATE TABLE IF NOT EXISTS produits (
    id BIGSERIAL PRIMARY KEY,
    declaration_id BIGINT NOT NULL REFERENCES declarations(id) ON DELETE CASCADE,
    libelle VARCHAR(255) NOT NULL,
    montant NUMERIC(15, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS charges (
    id BIGSERIAL PRIMARY KEY,
    declaration_id BIGINT NOT NULL REFERENCES declarations(id) ON DELETE CASCADE,
    libelle VARCHAR(255) NOT NULL,
    montant NUMERIC(15, 2) NOT NULL
);
