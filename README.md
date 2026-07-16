# Forge — AI Fitness Platform

[![CI](https://github.com/isharak7m/forge/actions/workflows/ci.yml/badge.svg)](https://github.com/isharak7m/forge/actions/workflows/ci.yml)

AI-powered personal fitness analytics with workout tracking, nutrition logging, sleep/water tracking, and ML-based predictions.

## Architecture

```
forge/
├── frontend/          # React + Vite + TypeScript + Tailwind
│   └── src/
│       ├── api/       # API client layer
│       ├── components/# UI components
│       ├── pages/     # Route pages
│       └── store/     # State management (zustand)
│
├── src/               # Spring Boot 3 + Java 25
│   ├── controller/    # REST endpoints
│   ├── service/       # Business logic
│   ├── ai/            # ML models & predictions
│   ├── entity/        # JPA entities
│   └── security/      # JWT auth
│
├── Dockerfile         # Backend container
├── render.yaml        # Render deployment
└── .github/workflows/ # CI pipeline
```

## Quick Start

### Prerequisites
- Java 25, Node.js 20, PostgreSQL

### Backend
```bash
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Environment Variables
| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | JWT signing key |

## Deployment

- **Frontend**: Vercel (auto-deploys from `master`)
- **Backend**: Render (auto-deploys from `master`)
- **Database**: Render PostgreSQL

## License

MIT
