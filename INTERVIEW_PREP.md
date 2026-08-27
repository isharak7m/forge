# Forge — Interview Preparation Guide

Master these to handle any recruiter/technical interviewer question. Organized by category with theory, logic, "why" explanations, and code snippets.

---

## 1. High-Level Architecture (Always Asked First)

**Your opening answer (memorize this):**
> "Forge is a full-stack AI fitness platform. The backend is Java 25 + Spring Boot 3.4 exposing 40+ REST endpoints across 9 controllers. The frontend is React 18 + TypeScript with Vite and Tailwind. It tracks workouts, nutrition, sleep, water, and weight, then uses custom ML models—that I built on my own linear algebra library called `jnumj`—to predict progress and detect plateaus. It's deployed with Docker on Render (backend + PostgreSQL) and Vercel (frontend), with a GitHub Actions CI pipeline."

### Potential questions:

**Q: Why Spring Boot?**
> Standard choice — mature ecosystem, auto-configuration, built-in security, JPA integration, easy REST controllers. Dependency injection, battle-tested, huge community.

**Q: Why React + TypeScript?**
> Component-based, massive ecosystem, good for a data-heavy dashboard UI. TypeScript adds type safety to prevent bugs, which is critical with complex data models (macros, micronutrients).

**Q: Why PostgreSQL?**
> Relational — fitness data has clear relationships (user → workouts → exercise logs). ACID compliance for financial-style data integrity (calorie tracking, progression). Robust, free, scales well.

**Q: Walk me through the layers (architecture layers)**
> Frontend (React) → Axios → REST API → Controller → Service → Repository (JPA) → PostgreSQL. Security filters run before controllers (JWT + rate limiting).

**Q: How does a request travel end to end?**
> User clicks "login" → LoginPage calls `authApi.login()` → Axios POST to `/api/auth/login` → Nginx/Vercel proxy → Spring's `RateLimitingFilter` checks IP quota → `JwtAuthenticationFilter` (for protected routes) validates token → `AuthController` → `AuthService` verifies BCrypt password → returns JWT → stored in Zustand + localStorage → React routes to `/dashboard`.

---

## 2. Authentication & Security

**Q: How does your JWT auth work?**
> On login, the server verifies the password with BCrypt and issues a JWT (header.payload.signature). The payload has user id + expiry. The client stores it in localStorage via Zustand. Every request, the Axios request interceptor adds `Authorization: Bearer <token>`. The `JwtAuthenticationFilter` (a `OncePerRequestFilter`) reads it, validates the signature and expiry, and sets the Spring Security context.

**Q: Why not store token in localStorage?** (trade-off question)
> LocalStorage is vulnerable to XSS (any injected script can read it). Cookies (especially HttpOnly) are safer but need CSRF protection. For this app, localStorage + strict CORS + JWT is a reasonable trade-off, but I acknowledge cookies are more secure.

**Q: What is stateless authentication?**
> The server stores no session. The token itself carries the identity. Anyone with a valid signature is authenticated. This scales horizontally — no shared session store needed.

**Q: How does the rate limiter work?**
> I wrote a custom sliding-window token bucket. A `ConcurrentHashMap<IP, ClientBucket>` tracks each IP. `AtomicInteger` counts requests. If a client exceeds 600 requests in 60 seconds, it returns HTTP 429. The `ConcurrentHashMap` and `AtomicInteger` make it thread-safe.

**Q: Why HTTP 429?**
> Standard status for "Too Many Requests". Let's the client know it's being rate-limited, not an error.

---

## 3. Custom ML Models (YOUR STRONGEST SECTION)

### 3a. Linear Regression (OLS — Ordinary Least Squares)

**Q: Explain your linear regression model.**
> I solve the normal equation `θ = (X^T X)^-1 X^T y` using matrix operations from `jnumj`. X is a design matrix with a column of 1s (for intercept) and the feature. This gives the slope and intercept that minimize squared error. I then compute R² to measure fit.

**Q: What is R²?**
> The coefficient of determination — measures how much of the variance is explained by the model. `R² = 1 - (SSres/SStot)`. Higher is better. In my code:
```java
rSquared = ssTot == 0 ? 1.0 : 1.0 - (ssRes / ssTot);
```

**Q: Why do you use the normal equation instead of gradient descent here?**
> For linear regression with few features, the normal equation is exact, fast, and needs no learning rate. Gradient descent is iterative; the closed form gives the exact answer in one matrix solve.

**Q: What's the O(n³) concern?**
> Computing `(X^T X)^-1` is cubic in the number of features. With 1-2 features it's trivial. If there were thousands of features, gradient descent would be better. I acknowledge this trade-off.

---

### 3b. Logistic Growth Model (gradient descent)

**Q: What is this model and why did you use it?**
> It's an S-curve (sigmoid) that models growth that plateaus — perfect for strength gains: rapid early progress, then tapering as you approach your ceiling. Formula: `y = L / (1 + e^(-k(t - t0)))` where L = asymptote (max), k = growth rate, t0 = inflection point.

**Q: How did you fit it?**
> Gradient descent with 8000 iterations. I compute partial derivatives of the loss w.r.t. L, k, and t0, then update each parameter: `L -= lr * 2 * mean(dL)`. I use `jnum` for the vector math.

**Q: Why 8000 iterations?**
> Empirically enough for the small dataset (exercise history) to converge. I clamped k and L to keep them physically meaningful (k > 0, L above max observed).

**Q: Why start L at `maxY * 1.3`?**
> A good heuristic — the growth ceiling is above the current max value. This gives gradient descent a sensible starting point, avoiding starting too low.

---

### 3c. Log Regression (1RM strength formulas)
**Q: What's that used for?**
> Fitting 1RM progression with a log curve `y = a·ln(x+1) + b`, capturing rapid early gains that slow over time. The Epley formula estimates 1RM: `1RM = weight × (1 + reps/30)`.

---

### 3d. Q-Learning (Reinforcement Learning)
**Q: How does your RL work?**
> An agent learns by trial-and-error. It maintains a **Q-table** (state → action → value). State is composed of (adherence tier, weight trend, has plateau). Actions include increase_protein, deload_week, add_cardio, etc. Uses **epsilon-greedy**: 85% of the time picks the best action (exploit), 15% explores random actions. Learning rate 0.1, discount factor 0.9.

**Q: What's the q-learning update rule?**
> `Q(s,a) ← Q(s,a) + α [r + γ·maxQ(s',a') - Q(s,a)]`. Alpha = learning rate, gamma = discount. This is the Bellman equation.

**Q: Why reinforcement learning over a simple if-else?**
> RL can adapt based on feedback ("reward"). As the user's adherence/threshold changes, decisions improve. My `POST /rl/feedback` endpoint lets the system learn from outcomes.

---

## 4. Design Patterns

**Q: What's the Strategy pattern? Where do you use it?**
> Encapsulate a family of algorithms behind an interface. My `AITool` interface has 7+ implementations (food search, nutrition analysis, muscle recovery, etc.). The `ToolRegistry` auto-discovers all beans (via @Component) and the `ToolOrchestrator` picks the right tool per request. I can add a new tool without changing existing code — that's the open-closed principle.

**Q: Other patterns you used?**
> Builder (Lombok @Builder for DTOs/entities), Service layer, Repository pattern, DTO pattern, Singleton (Spring beans by default), Factory (ToolRegistry), Adapter.

---

## 5. Nutrition Domain Logic

**Q: How do you calculate daily totals/daily macros?**
> The frontend queries `GET /nutrition/analytics/daily` which aggregates all food logs for a date, summing calories, protein, carbs, fat. The RecommendationService calculates TDEE via the Mifflin-St Jeor equation:
> `BMR = 10·weight + 6.25·height - 5·age + 5 (male) or -161 (female)`, then multiplies by an activity factor.

**Q: What's the serving size scaling?**
> Food items store per-100g nutrition. When the user picks a serving size and unit, the frontend scales: `values × (serving/100)`. So 250g of a food with 165 kcal/100g → 412.5 kcal.

**Q: Why track micronutrients?**
> Precision and differentiation. Users see vitamin/mineral breakdown (RDA tracking), making the product more complete than calorie-only trackers.

---

## 6. Frontend Engineering

**Q: How does the frontend handle auth?**
> Zustand `authStore` with persist middleware persists the token in localStorage. Axios interceptors: request interceptor adds the Bearer token and checks expiry (redirects to /login if expired); response interceptor handles 401 globby by logging out. The `ProtectedRoute` component wraps authenticated routes.

**Q: Why is there Auto-login on register?**
> The register endpoint returns a JWT immediately, so the UI logs them in and routes to Dashboard for a smooth onboarding. (Your earlier design choice.)

**Q: What is the optimizer vs optimistic update?**
> In NutritionPage, when logging food, it optimistically updates the UI (adds the entry) before the server confirms, then calls the API and reloads. This feels instant.

**Q: What is a PWA?**
> It's a web app installable on mobile, with a service worker enabling offline support and caching. The `vite-plugin-pwa` with Workbox handles caching.

**Q: What is `useMouseParallax>` and the 3D scene?**
> A custom hook using `requestAnimationFrame` for GPU-accelerated mouse-driven parallax tilt. The 3D visuals (dumbbell, spheres) use React Three Fiber for a premium look.

---

## 7. Database Design

**Q: What tables do you have?**
> users, workout_sessions, exercise_logs, food_logs, sleep_logs, water_logs, weight_logs, food_items. Each linked to a user via user_id FK.

**Q: How do you manage migrations?**
> Flyway. Versioned SQL files (V1, V2, ...) run automatically on startup. I later moved to `ddl-auto: update` + Flyway to fix schema mismatches that arose from column naming.

**Q: What was the schema challenge?**
> The JPA naming strategy produced `carbs_per100g` but the migration had `carbs_per_100g`. I fixed it with a V5 migration renaming columns and aligning entity naming. This taught me to always match migrations exactly to entity mappings.

---

## 8. Testing & CI

**Q: What tests do you have?**
> Backend: JUnit 5 + Mockito — AuthServiceTest (register+login), PlateauDetectionServiceTest, LogRegressionTest, HealthCheckTest (integration with MockMvc hitting /actuator/health). Frontend: Vitest.

**Q: What is CI/CD?**
> GitHub Actions runs on every push: builds the Maven backend, installs frontend, runs type-check, tests, and build. Ensures nothing breaks before deploy.

**Q: How is it deployed?**
> Multi-stage Docker builds (Maven→JRE for backend, Node→Nginx for frontend). Render auto-deploys backend + PostgreSQL; Vercel auto-deploys the frontend on push to master.

---

## 9. Scalability / "How would you improve?"

Good "gotcha" questions you should have answers to:

**Q: How would you scale this?**
> 1) Add Redis caching for the food database and dashboard aggregates. 2) Add database indexes on frequently-queried columns (user_id, date). 3) Move ML predictions to async jobs (processing). 4) Horizontally scale: JWT/stateless makes it easy to add instances behind a load balancer. 5) Use connection pooling.

**Q: What are security vulnerabilities?**
> - Use HttpOnly cookies instead of localStorage for JWT to reduce XSS risk.
> - Add CSRF protection if using cookies.
> - Sanitize food search input (already length-limited but add more validation).
> - Properly mask/rotate secrets (never hardcode).

**Q: What would you do differently?**
> 1) Better test coverage for frontend. 2) Keep secrets totally out of code. 3) Use Redis for caching early on. 4) Make the JPA/migration naming consistent.

---

## 10. Quick Snippet Exercises

**Snippet 1: How do you handle a 401?**
```java
// Axios response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

**Snippet 2: Predict fit**
```java
// LinearRegressionModel.predict()
public double predict(double x) { return slope * x + intercept; }
```

**Snippet 3: Design matrix**
```java
double[][] xMat = new double[n][2]; // column of 1's + real
```

---

Final note: **The interviewer cares about that** — you know the math, can whiteboard, explain tradeoffs, and can discuss its. Practice the ML section deeply. Good luck.