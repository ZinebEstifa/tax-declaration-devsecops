# 🏛️ SIMPL-IS — Plateforme de Télédéclaration et Télépaiement de l'Impôt sur les Sociétés (DGI Maroc)

Bienvenue dans le dépôt du projet **SIMPL-IS**, la solution web sécurisée de télédéclaration et télépaiement de l'Impôt sur les Sociétés (IS) développée selon les meilleures pratiques **DevSecOps** et les normes de la Direction Générale des Impôts (DGI Maroc).

---

## 📌 Sommaire
1. [Présentation du Projet](#-présentation-du-projet)
2. [Architectures et Choix Techniques](#-architectures-et-choix-techniques)
3. [Sécurité et Cryptographie (Argon2id & JWT)](#-sécurité-et-cryptographie-argon2id--jwt)
4. [Prérequis Système](#-prérequis-système)
5. [Guide d'Installation et d'Exécution](#-guide-dinstallation-et-dexécution)
   - [Backend (Spring Boot)](#1-démarrage-du-backend-spring-boot)
   - [Frontend (Angular)](#2-démarrage-du-frontend-angular)
6. [Guide d'Utilisation de l'Application](#-guide-dutilisation-de-lapplication)
7. [Consultation de la Base de Données (H2 Console)](#-consultation-de-la-base-de-données-h2-console)
8. [Déploiement en Production (PostgreSQL)](#-déploiement-en-production-postgresql)
9. [Tests d'Intégration & Sécurité](#-tests-dintégration--sécurité)

---

## 🇲🇦 Présentation du Projet

L'application **SIMPL-IS** permet aux contribuables et sociétés marocaines d'effectuer l'ensemble de leurs obligations fiscales relatives à l'Impôt sur les Sociétés en ligne :
- **Authentification sécurisée** par Identifiant Fiscal (IF).
- **Auto-inscription et connexion fluide** pour tout contribuable.
- **Saisie dynamique des Produits et des Charges** de l'exercice comptable.
- **Calcul automatique et conforme DGI** du Résultat Comptable, Résultat Fiscal, Cotisation Minimale (0.5%), et du montant de l'Impôt sur les Sociétés (IS) à payer.
- **Dépôt, Validation et Rectification** des déclarations fiscales avec conservation historique.

---

## 🛠️ Architectures et Choix Techniques

### 🔹 Backend (API RESTful)
- **Langage / Framework** : Java 17, Spring Boot 3.4 / 4.1.
- **Sécurité** : Spring Security 6, Token JWT (HMAC-256).
- **Hachage Mot de Passe** : **Argon2id** (Argon2BytesGenerator BouncyCastle).
- **ORM / Persistance** : Spring Data JPA, Hibernate.
- **Gestion des Dépendances** : Apache Maven (Wrapper `mvnw`).

### 🔹 Frontend (Single Page Application)
- **Framework** : Angular 19 (TypeScript, RxJS).
- **Style & UI** : Vanilla CSS custom aux couleurs et normes graphiques officielles de la DGI Maroc (Bleu Roi, Or, Blanc pur).
- **Architecture des composants** :
  - `LoginComponent` : Authentification et auto-inscription.
  - `ExerciceComponent` : Stepper interactif en 3 étapes (Produits ➔ Charges ➔ Déclaration & Calcul).
  - `DeclarationComponent` : Historique des déclarations, validation et rectification.

---

## 🔒 Sécurité et Cryptographie (Argon2id & JWT)

Conformément au cahier des charges DevSecOps et aux recommandations ANRT/DGI :

1. **Algorithme Argon2id** :
   - **Mode** : Argon2id (hybride résistant aux attaques par canal auxiliaire et aux GPU/ASIC).
   - **Paramètres** : 3 itérations, 16 MB (16384 KB) de mémoire vive, 4 threads de parallélisme.
   - **Salt (Sel)** : 16 octets aléatoires générés de manière cryptographiquement forte et stockés de façon unique par utilisateur.
   - **Pepper (Poivre)** : Clé secrète côté serveur intégrée avant le hachage.
2. **Protection Bruteforce** :
   - Compteur de tentatives échouées. Le compte utilisateur est **automatiquement bloqué** après 3 tentatives de mot de passe incorrects.
3. **Jetons JWT (JSON Web Token)** :
   - Signature cryptographique **HMAC-256**.
   - Injection automatique de l'en-tête `Authorization: Bearer <token>` sur le frontend via `jwtInterceptor`.
   - Expiration automatique et redirection en cas de session expirée (HTTP 401/403).

---

## 💻 Prérequis Système

Assurez-vous d'avoir installé sur votre machine :
- **Java JDK 17** (ou version plus récente). Vérifier avec `java -version`.
- **Node.js v18+** et **npm**. Vérifier avec `node -v` et `npm -v`.
- **Git** pour le suivi de version.

---

## 🚀 Guide d'Installation et d'Exécution

### 1️⃣ Cloner le Dépôt GitHub

```bash
git https://github.com/ZinebEstifa/tax-declaration-devsecops.git
cd Declarations-fiscaux
```

---

### 2️⃣ Démarrage du Backend (Spring Boot)

Naviguez dans le dossier `backend` :

```bash
cd backend
```

#### A. Exécuter les tests automatisés (Recommandé) :
```bash
# Sur Windows (PowerShell / CMD)
.\mvnw clean test

# Sur Linux / macOS
./mvnw clean test
```
*Cette commande exécute l'intégralité des 21 tests d'intégration et de sécurité (Hachage Argon2id, Authentification JWT, Calculs IS, Persistance).*

#### B. Lancer le serveur backend :
```bash
# Sur Windows
.\mvnw spring-boot:run

# Sur Linux / macOS
./mvnw spring-boot:run
```
Le serveur backend démarre sur **`http://localhost:8080`**.

---

### 3️⃣ Démarrage du Frontend (Angular)

Ouvrez un **nouveau terminal**, puis naviguez dans le dossier `frontend` :

```bash
cd Declarations-fiscaux/frontend
```

#### A. Installer les dépendances Node.js :
```bash
npm install
```

#### B. Lancer le serveur de développement Angular :
```bash
npx ng serve
```
Le serveur frontend démarre sur **`http://localhost:4200`**.

---

## 🎯 Guide d'Utilisation de l'Application

1. Ouvrez votre navigateur sur **`http://localhost:4200`**.
2. **Authentification / Inscription** :
   - Saisissez **n'importe quel Identifiant Fiscal (IF)** (ex: `12345678` ou `87309470`) et n'importe quel mot de passe.
   - Si l'IF n'existe pas encore en base de données, le système **crée automatiquement le compte contribuable** et vous connecte directement.
3. **Télédéclaration d'Exercice (Stepper en 3 Étapes)** :
   - **Étape 1 — Produits** : Saisissez vos produits d'exploitation et produits financiers.
   - **Étape 2 — Charges** : Saisissez vos charges d'exploitation, salaires, loyers et impôts.
   - **Étape 3 — Déclaration** :
     - Visualisez le Résultat Comptable et le Résultat Fiscal.
     - L'application calcule automatiquement la **Cotisation Minimale (0.5%)** et le montant de l'**Impôt sur les Sociétés (IS)**.
     - Cliquez sur **Déposer la Déclaration** ou **Valider**.
4. **Rectification** :
   - En cas d'erreur sur une déclaration déposée, utilisez l'option de rectification pour soumettre une version corrigée. Le système calcule automatiquement l'écart de régularisation.

---

## 🗄️ Consultation de la Base de Données (H2 Console)

En mode développement, l'application utilise une base de données **H2 persistante sur fichier** (`~/.taxdb/taxdb`), ce qui garantit la conservation intégrale des données même après le redémarrage du backend.

Pour explorer les tables en direct :
1. Rendez-vous sur **[`http://localhost:8080/h2-console/`](http://localhost:8080/h2-console/)** *(notez le `/` final)*.
2. Renseignez les identifiants :
   - **JDBC URL** : `jdbc:h2:file:~/.taxdb/taxdb`
   - **User Name** : `sa`
   - **Password** : *(laisser vide)*
3. Cliquez sur **Connect**.

#### Requêtes SQL utiles :
```sql
-- Consulter la liste des contribuables et leurs hashs Argon2id
SELECT * FROM UTILISATEURS;

-- Consulter les déclarations fiscales enregistrées
SELECT * FROM DECLARATION;

-- Consulter le détail des produits et des charges
SELECT * FROM PRODUIT;
SELECT * FROM CHARGE;
```

---

## 🐘 Déploiement en Production (PostgreSQL)

L'application est prête pour un déploiement d'entreprise sur PostgreSQL.

1. **Script DDL PostgreSQL** : Le fichier `backend/src/main/resources/schema.sql` contient le schéma DDL natif PostgreSQL avec contraintes d'intégrité et clés étrangères.
2. **Profil de Production** :
   Pour démarrer avec une base de données PostgreSQL de production :
   ```bash
   .\mvnw spring-boot:run -Dspring-boot.run.profiles=prod
   ```
   *Assurez-vous de configurer vos identifiants PostgreSQL dans `application-prod.properties`.*

---

## 🧪 Tests d'Intégration & Sécurité

La suite de tests unitaires et d'intégration couvre :
- `Argon2idPasswordEncoderTest` : Vérification du hachage, du sel aléatoire et de la correspondance des mots de passe.
- `AuthServiceTest` : Inscription, connexion, verrouillage de compte après 3 échecs.
- `DeclarationServiceTest` : Calcul des barèmes d'imposition IS et de la Cotisation Minimale.
- `ProduitServiceTest` & `ChargeServiceTest` : Gestion du CRUD des éléments comptables.

Exécution des tests :
```bash
cd backend
.\mvnw test
```

---


