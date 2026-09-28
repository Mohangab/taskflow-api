# TaskFlow Frontend

Angular 19 SPA for the [TaskFlow API](../README.md).

## Quick start

```bash
# from repo root — start the API first
mvn spring-boot:run

# then in another terminal
cd frontend
npm install
npm start
```

App: http://localhost:4200  
API: http://localhost:8080 (`environment.apiUrl`)

## Scripts

| Command | Description |
|---------|-------------|
| `npm start` | Dev server (`ng serve` on port 4200) |
| `npm run build` | Production build → `dist/frontend` |
| `npm test` | Unit tests (Karma) |

## Architecture

- **Auth** — register/login → JWT in `localStorage`; `authInterceptor` attaches `Authorization: Bearer <token>`
- **Guards** — `authGuard` protects `/tasks`; `guestGuard` redirects signed-in users away from login/register
- **Tasks** — list / create / update status / delete / filter

## Environments

- `src/environments/environment.development.ts` — used by `ng serve` (`apiUrl: http://localhost:8080`)
- `src/environments/environment.ts` — production default (same API URL; set `apiUrl: ''` when reverse-proxying `/api`)
