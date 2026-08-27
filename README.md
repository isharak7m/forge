# Forge — AI Fitness Platform

[![CI](https://github.com/isharak7m/forge/actions/workflows/ci.yml/badge.svg)](https://github.com/isharak7m/forge/actions/workflows/ci.yml)

Full-stack AI fitness platform that tracks workouts, nutrition, sleep, water, and weight — then uses custom ML models to predict progress, detect plateaus, and give personalized recommendations.

Built by [isharak7m](https://github.com/isharak7m).

---

## Live Demo

- **Frontend**: [forge-eosin-gamma.vercel.app](https://forge-eosin-gamma.vercel.app)
- **Backend API**: [forge-w40c.onrender.com/api](https://forge-w40c.onrender.com/api)

---

## What It Does

| Feature | Description |
|---|---|
| **Workout Tracking** | Log sessions, sets, reps, weight. Calculate 1RM estimates. |
| **Nutrition Logging** | Search 1000+ curated foods, log meals, track macros. |
| **Sleep Tracking** | Log sleep hours, get quality analysis. |
| **Water Tracking** | Daily water intake logging. |
| **Weight Tracking** | Weight history with trend visualization. |
| **AI Predictions** | Linear regression to predict weight goals. |
| **Plateau Detection** | Detects when progress stalls and alerts you. |
| **Workout Recommendations** | Suggests next workout based on history. |
| **Adherence Scoring** | Measures how consistent you are with goals. |

---

## Tech Stack

### Backend
- **Java 25** + **Spring Boot 3.4.5**
- **Spring Security** — JWT authentication
- **Spring Data JPA** — ORM / database access
- **Flyway** — database migrations
- **PostgreSQL** (Supabase) — production database
- **Docker** — containerized deployment

### Frontend
- **React 18** + **TypeScript**
- **Vite** — build tool
- **Tailwind CSS** — styling
- **Zustand** — state management
- **Recharts** — charts / data visualization

### ML / AI
- **jnumj** — my custom numerical computing library for the JVM (like NumPy but for Java). Used for matrix operations, linear algebra, and gradient descent.
- **Custom ML models** — linear regression, logistic growth curves, plateau detection, adherence scoring

### Deployment
- **Backend**: Render (Docker container)
- **Frontend**: Vercel
- **Database**: Supabase PostgreSQL (via connection pooler)
- **CI/CD**: GitHub Actions

---

## Architecture

```
forge/
├── frontend/                    # React + Vite + TypeScript + Tailwind
│   └── src/
│       ├── api/                 # Axios client + auth endpoints
│       ├── components/          # Reusable UI components
│       ├── pages/               # Route pages (Dashboard, Workouts, Nutrition, etc.)
│       ├── store/               # Zustand state management
│       └── lib/                 # Utility functions
│
├── src/main/java/com/forge/     # Spring Boot backend
│   ├── controller/              # REST endpoints (9 controllers, 40+ endpoints)
│   ├── service/                 # Business logic
│   ├── ai/ml/                   # ML models (linear regression, plateau detection, etc.)
│   ├── ai/tool/                 # AI tools (meal planner, exercise analytics, etc.)
│   ├── ai/recommendation/       # Recommendation engine
│   ├── entity/                  # JPA entities + enums
│   ├── repository/              # Spring Data repositories
│   ├── dto/                     # Request/Response DTOs
│   ├── security/                # JWT filter, token provider, rate limiting
│   └── config/                  # CORS, OpenAPI, seeders
│
├── src/main/resources/
│   ├── application.yml          # App config (env vars for secrets)
│   └── db/migration/            # Flyway SQL migrations
│
├── Dockerfile                   # Backend container
├── render.yaml                  # Render deployment config
└── .github/workflows/ci.yml    # CI pipeline
```

---

## Getting Started

### Prerequisites
- Java 25
- Node.js 20+
- PostgreSQL

### 1. Clone the repo
```bash
git clone https://github.com/isharak7m/forge.git
cd forge
```

### 2. Backend
```bash
# Set environment variables (or use .env)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/forge
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your-long-random-secret-at-least-32-chars

# Run
./mvnw spring-boot:run
```

Backend starts at `http://localhost:8080`

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend starts at `http://localhost:5173`

---

## Environment Variables

| Variable | Where | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Render env | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | Render env | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Render env | Database password |
| `JWT_SECRET` | Render env | JWT signing key (min 32 chars) |

> **Never commit secrets.** These are set in the Render dashboard, not in code.

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/users/me` | Get current user profile |
| PUT | `/api/users/me` | Update profile |
| GET | `/api/dashboard` | Daily dashboard summary |
| GET | `/api/dashboard/weekly` | Weekly summary |
| GET | `/api/dashboard/monthly` | Monthly summary |
| POST | `/api/workouts` | Log workout session |
| GET | `/api/workouts` | Get workout history |
| POST | `/api/nutrition/log` | Log food intake |
| GET | `/api/nutrition/foods/search` | Search food database |
| GET | `/api/nutrition/summary` | Daily nutrition summary |
| POST | `/api/sleep` | Log sleep |
| POST | `/api/water` | Log water intake |
| POST | `/api/weight` | Log weight |
| GET | `/api/weight/history` | Weight history |
| GET | `/api/ai/predictions` | Weight predictions |
| GET | `/api/ai/plateaus` | Plateau detection |
| GET | `/api/ai/recommendations` | Workout recommendations |
| GET | `/api/ai/adherence` | Adherence score |

---

## How the ML Works

All ML models are built on **jnumj**, my custom linear algebra library for Java.

### Linear Regression (Weight Prediction)
Solves the normal equation `θ = (X^T X)^-1 X^T y` using jnumj matrix operations. Predicts when you'll hit your target weight.

### Plateau Detection
Uses a sliding window approach — if the slope of recent data points is near zero, it flags a plateau.

### Workout Recommendations
Analyzes your exercise history, muscle group coverage, and progressive overload to suggest what to do next.

---

## License

MIT

---

## Author

**Isharak** — [GitHub](https://github.com/isharak7m)
