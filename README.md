# Budget Tracker API

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.7-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white)
![Tink](https://img.shields.io/badge/Open_Banking-Tink_PSD2-0057FF?style=flat)

Application REST de suivi de budget personnel avec connexion bancaire Open Banking via l'API Tink PSD2. Permet d'importer automatiquement ses transactions bancaires, de les catégoriser, et de consulter des statistiques de dépenses par période.

---

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Runtime | Java 21 |
| Framework | Spring Boot 4.0.7 |
| Base de données | PostgreSQL 16 |
| Migration BDD | Flyway |
| Authentification | JWT (JJWT 0.12.3) + Spring Security |
| Open Banking | Tink API PSD2 |
| Containerisation | Docker Compose |
| Build | Maven Wrapper (`mvnw`) |
| Utilitaires | Lombok |

---

## Architecture des packages

```
com.ayoub.budgettracker
├── controller/         # Endpoints REST (Auth, User, Account, Category, Transaction, Stats, Tink)
├── service/            # Logique métier
├── repository/         # Spring Data JPA + Specifications
├── entity/             # Entités JPA (User, Account, Category, Transaction)
├── dto/
│   ├── request/        # Payloads entrants (Register, Login, UpdateProfile, ChangePassword…)
│   ├── response/       # Payloads sortants (Auth, Transaction, Stats, PagedResponse…)
│   └── tink/           # DTOs de désérialisation des réponses Tink
├── mapper/             # Conversions entité ↔ DTO
├── specification/      # JPA Specifications (filtres dynamiques transactions)
├── scheduler/          # Jobs planifiés (sync Tink nuitamment)
├── security/           # JWT filter, UserDetailsService, config Spring Security
├── config/             # Beans de configuration
└── exception/          # Gestion centralisée des erreurs
```

---

## Lancer le projet

### Prérequis

- Docker Desktop
- Java 21
- Maven (ou utiliser `./mvnw`)

### 1. Démarrer PostgreSQL

```bash
docker-compose up -d
```

Cela démarre un conteneur PostgreSQL sur le port `5432` avec la base `budget_tracker_dev`.

### 2. Lancer l'API

```bash
./mvnw spring-boot:run
```

L'API est disponible sur `http://localhost:8080`.  
Flyway applique automatiquement les migrations au démarrage.

### Variables d'environnement (optionnel)

Les valeurs par défaut sont dans `src/main/resources/application.yaml`.  
Pour les surcharger :

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/budget_tracker_dev
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export APP_JWT_SECRET=votre_secret_256bits
export TINK_CLIENT_ID=votre_client_id
export TINK_CLIENT_SECRET=votre_client_secret
```

---

## Endpoints API

### Authentification — `/api/auth`

| Méthode | Route | Description |
|---------|-------|-------------|
| `POST` | `/api/auth/register` | Inscription + création des catégories par défaut |
| `POST` | `/api/auth/login` | Connexion → JWT |

### Profil utilisateur — `/api/users`

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/users/me` | Infos du compte connecté |
| `PUT` | `/api/users/me` | Modifier prénom / nom |
| `PUT` | `/api/users/me/password` | Changer le mot de passe |

### Comptes bancaires — `/api/accounts`

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/accounts` | Liste des comptes |
| `POST` | `/api/accounts` | Créer un compte |
| `DELETE` | `/api/accounts/{id}` | Supprimer un compte |

### Catégories — `/api/categories`

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/categories` | Liste des catégories |
| `POST` | `/api/categories` | Créer une catégorie |
| `DELETE` | `/api/categories/{id}` | Supprimer une catégorie |

### Transactions — `/api/transactions`

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/transactions` | Liste paginée avec filtres |
| `POST` | `/api/transactions` | Créer une transaction |
| `PATCH` | `/api/transactions/{id}/category` | Modifier la catégorie |
| `DELETE` | `/api/transactions/{id}` | Supprimer une transaction |

**Filtres disponibles sur `GET /api/transactions` :**

```
?type=EXPENSE&categoryId=uuid&from=2026-01-01&to=2026-06-30&page=0&size=20
```

### Statistiques — `/api/stats`

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/stats/by-category` | Dépenses du mois par catégorie |
| `GET` | `/api/stats/monthly` | Revenus / dépenses des 6 derniers mois |
| `GET` | `/api/stats/balance` | Solde, total revenus/dépenses du mois |

### Open Banking Tink — `/api/tink`

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/tink/connect` | Génère l'URL Tink Link (redirection bancaire) |
| `POST` | `/api/tink/import` | Importe les transactions depuis un code Tink |

---

## Fonctionnalités

### Gestion de budget
- Création de comptes bancaires (courant, épargne…)
- Transactions manuelles INCOME / EXPENSE avec catégorie et date
- Filtres combinés dynamiques : type, catégorie, période, pagination

### Catégorisation automatique
- 10 catégories par défaut créées à l'inscription (Alimentation, Transport, Logement, Loisirs, Sport, Santé, Shopping, Salaire, Épargne, Divers)
- Mapping automatique MCC (Merchant Category Code) → catégorie lors de l'import Tink
- Fallback par pattern matching sur le nom du marchand si MCC absent

### Open Banking (Tink PSD2)
- Connexion bancaire via Tink Link (flux OAuth PSD2)
- Import des transactions des 3 derniers mois avec déduplication par `tink_id`
- Synchronisation automatique toutes les nuits à 2h (`@Scheduled`)

### Sécurité
- Authentification stateless par JWT (24h d'expiration)
- Chaque ressource est isolée par utilisateur (`@AuthenticationPrincipal`)
- Changement de mot de passe avec vérification bcrypt de l'ancien

### Statistiques
- Répartition des dépenses par catégorie (mois courant)
- Évolution revenus / dépenses sur 6 mois glissants
- Balance mensuelle : revenus, dépenses, épargne nette, nombre de transactions

---

## Migrations Flyway

| Version | Description |
|---------|-------------|
| V1 | Création table `users` |
| V2 | Création table `accounts` |
| V3 | Création table `categories` |
| V4 | Création table `transactions` |
| V5 | Ajout colonnes `tink_user_id` et `tink_id` |
| V6 | Ajout colonne `type` sur `categories` |
| V7 | Index unique partiel sur `tink_id` (`WHERE tink_id IS NOT NULL`) |
