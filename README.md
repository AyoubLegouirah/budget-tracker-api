# Budget Tracker API

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.7-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white)
![Tink](https://img.shields.io/badge/Open_Banking-Tink_PSD2-0057FF?style=flat)

A personal finance REST API with Open Banking integration via the Tink PSD2 API. Automatically imports bank transactions, categorizes them, and provides spending statistics, budget tracking, recurring subscription detection, and monthly PDF reports.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 4.0.7 |
| Database | PostgreSQL 16 |
| DB Migrations | Flyway |
| Authentication | JWT (JJWT 0.12.3) + Spring Security |
| Open Banking | Tink API PSD2 |
| PDF Generation | Apache PDFBox 3.0.3 |
| Containerization | Docker Compose |
| Build | Maven Wrapper (`mvnw`) |
| Utilities | Lombok |

---

## Package Structure

```
com.ayoub.budgettracker
├── controller/         # REST endpoints (Auth, User, Account, Category, Transaction, Stats, Budget, Report, Tink)
├── service/            # Business logic
├── repository/         # Spring Data JPA + Specifications
├── entity/             # JPA entities (User, Account, Category, Transaction, Budget)
├── dto/
│   ├── request/        # Inbound payloads (Register, Login, UpdateProfile, ChangePassword, BudgetRequest…)
│   ├── response/       # Outbound payloads (Auth, Transaction, Stats, Budget, PagedResponse…)
│   └── tink/           # Tink API response DTOs
├── mapper/             # Entity ↔ DTO conversions
├── specification/      # JPA Specifications (dynamic transaction filters)
├── scheduler/          # Scheduled jobs (nightly Tink sync)
├── security/           # JWT filter, UserDetailsService, Spring Security config
├── config/             # Configuration beans
└── exception/          # Centralized error handling
```

---

## Getting Started

### Prerequisites

- Docker Desktop
- Java 21
- Maven (or use `./mvnw`)

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

Starts a PostgreSQL container on port `5432` with the `budget_tracker_dev` database.

### 2. Run the API

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.  
Flyway migrations are applied automatically on startup.

### Environment Variables (optional)

Default values are set in `src/main/resources/application.yaml`.  
Override with:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/budget_tracker_dev
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export APP_JWT_SECRET=your_256bit_secret
export TINK_CLIENT_ID=your_client_id
export TINK_CLIENT_SECRET=your_client_secret
```

---

## API Endpoints

All endpoints except `/api/auth/**` require a JWT token in the `Authorization: Bearer <token>` header.

### Authentication — `/api/auth`

| Method | Route | Description |
|--------|-------|-------------|
| `POST` | `/api/auth/register` | Register + create default categories |
| `POST` | `/api/auth/login` | Login → returns JWT |

### User Profile — `/api/users`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/users/me` | Get current user info |
| `PUT` | `/api/users/me` | Update first name / last name |
| `PUT` | `/api/users/me/password` | Change password |

### Bank Accounts — `/api/accounts`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/accounts` | List accounts |
| `POST` | `/api/accounts` | Create an account |
| `DELETE` | `/api/accounts/{id}` | Delete an account |

### Categories — `/api/categories`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/categories` | List categories |
| `POST` | `/api/categories` | Create a category |
| `DELETE` | `/api/categories/{id}` | Delete a category |

### Transactions — `/api/transactions`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/transactions` | Paginated list with filters |
| `GET` | `/api/transactions/recurring` | Detect recurring transactions (subscriptions) |
| `POST` | `/api/transactions` | Create a transaction |
| `PATCH` | `/api/transactions/{id}/category` | Update transaction category |
| `DELETE` | `/api/transactions/{id}` | Delete a transaction |

**Available filters on `GET /api/transactions`:**

```
?type=EXPENSE&categoryId=uuid&from=2026-01-01&to=2026-06-30&page=0&size=20
```

**Response shape for `GET /api/transactions/recurring`:**

```json
[
  {
    "description": "Netflix",
    "monthlyAmount": 17.99,
    "type": "EXPENSE",
    "categoryName": "Loisirs",
    "categoryColor": "#6366f1",
    "categoryIcon": "tv",
    "frequency": "MONTHLY",
    "lastDate": "2026-06-14"
  }
]
```

### Statistics — `/api/stats`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/stats/by-category` | Expenses by category for a given month |
| `GET` | `/api/stats/monthly` | Revenue / expenses over the last 6 months |
| `GET` | `/api/stats/balance` | Monthly balance: income, expenses, net savings |

`by-category` and `balance` accept an optional `?month=YYYY-MM` parameter.  
If omitted, defaults to the current month.

```
GET /api/stats/balance?month=2026-01
GET /api/stats/by-category?month=2026-01
```

### Budgets — `/api/budgets`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/budgets` | List all budgets for the current user |
| `POST` | `/api/budgets` | Create or update a budget for a category |
| `DELETE` | `/api/budgets/{id}` | Delete a budget |
| `GET` | `/api/budgets/summary` | Budget vs. actual spending summary for a month |

`POST /api/budgets` is an upsert: if a budget already exists for the given category, it updates the amount.

**Request body for `POST /api/budgets`:**

```json
{ "categoryId": "uuid", "amount": 500.00 }
```

**Response shape for `GET /api/budgets/summary?month=2026-06`:**

```json
[
  {
    "categoryId": "uuid",
    "categoryName": "Logement",
    "categoryColor": "#6366f1",
    "categoryIcon": "home",
    "budgetAmount": 1200.00,
    "spentAmount": 950.00,
    "percentage": 79.2,
    "isOverBudget": false
  }
]
```

### Reports — `/api/reports`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/reports/monthly` | Generate and download a monthly PDF report |

```
GET /api/reports/monthly?month=2026-06
```

Returns `Content-Type: application/pdf` with `Content-Disposition: attachment; filename=bilan-2026-06.pdf`.  
If `month` is omitted, defaults to the current month.

### Open Banking Tink — `/api/tink`

| Method | Route | Description |
|--------|-------|-------------|
| `GET` | `/api/tink/connect` | Generate Tink Link URL (bank redirect) |
| `POST` | `/api/tink/import` | Import transactions using a Tink authorization code |

---

## Features

### Transaction Management
- Manual INCOME / EXPENSE transactions with category and date
- Dynamic combined filters: type, category, date range, pagination
- Per-user data isolation via `@AuthenticationPrincipal`

### Automatic Categorization
- 10 default categories created on registration (Food, Transport, Housing, Leisure, Sport, Health, Shopping, Salary, Savings, Misc)
- Automatic MCC (Merchant Category Code) → category mapping on Tink import
- Pattern matching fallback on merchant name when MCC is absent

### Open Banking (Tink PSD2)
- Bank connection via Tink Link (OAuth PSD2 flow)
- Imports transactions from the last 3 months with deduplication by `tink_id`
- Automatic nightly sync at 2 AM (`@Scheduled`)

### Budget Tracking
- Monthly spending limits per category
- Upsert semantics: one budget per (user, category) pair enforced by a unique constraint
- Summary endpoint computes `spentAmount`, `percentage`, and `isOverBudget` for a given month

### Recurring Transaction Detection
`GET /api/transactions/recurring` runs the following algorithm:

1. Groups all user transactions by description (case-insensitive, trimmed)
2. Computes the median amount per group and filters transactions within **±5%** of it
3. Requires **at least 3 occurrences on distinct calendar months**
4. Computes frequency from average month interval between occurrences
5. Keeps only **MONTHLY** or **ANNUAL** frequencies; drops IRREGULAR, QUARTERLY, etc.
6. Excludes descriptions containing `retrait`, `virement`, or `remboursement`
7. Returns results sorted by amount descending

### Monthly PDF Report
`GET /api/reports/monthly?month=YYYY-MM` generates a PDF using **Apache PDFBox 3.0.3** containing:

- **Header** — "BudgetTracker — Bilan de \<Month\> \<Year\>" + generation date
- **Summary** — total income, total expenses, net savings (3 side-by-side boxes, color-coded)
- **Expenses by category** — one line per category with dot leaders and percentage of total:
  ```
  Logement .......................... 1 300.00 EUR  (48.7%)
  ```
- **Last 50 transactions** — two lines per entry, amount right-aligned:
  ```
  14/06/2026  -  Essence  (Transport)
                                            - 100.00 EUR
  ```
- Automatic page breaks with footer `Page X / N` on every page

### Security
- Stateless JWT authentication (24h expiry)
- All resources scoped to the authenticated user
- Password change requires bcrypt verification of the current password

### Statistics
- Expense breakdown by category with optional month filter (`?month=YYYY-MM`)
- Revenue / expense trend over rolling 6 months
- Monthly balance: income, expenses, net savings, transaction count, with optional month filter

---

## Flyway Migrations

| Version | Description |
|---------|-------------|
| V1 | Create `users` table |
| V2 | Create `accounts` table |
| V3 | Create `categories` table |
| V4 | Create `transactions` table |
| V5 | Add `tink_user_id` and `tink_id` columns |
| V6 | Add `type` column on `categories` |
| V7 | Partial unique index on `tink_id` (`WHERE tink_id IS NOT NULL`) |
| V8 | Create `budgets` table with unique constraint on `(user_id, category_id)` |
