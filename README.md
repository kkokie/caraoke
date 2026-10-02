# Caraoke

Share the memories songs hold. Mobile-first (Expo) + Spring Boot API + PostgreSQL.

```
caraoke/
├── backend/            Spring Boot 3.5 / Java 21 / Flyway / Postgres
├── mobile/             Expo (React Native + TypeScript)
└── docker-compose.yml  Local Postgres
```

## Setup in IntelliJ

1. **File → Open** the `caraoke/` folder. IntelliJ picks up `backend/pom.xml` as a Maven project.
   If it doesn't, right-click `backend/pom.xml` → *Add as Maven Project*.
2. **Project Structure → SDK:** Java 21.
3. Start Postgres: `docker compose up -d` (from the repo root).
4. Run config for `CaraokeApplication`:
   - Active profiles: `local` (turns on dev-mode auth)
5. Run it. Flyway creates the schema on first boot.
6. Run the tests: right-click `backend/src/test` → *Run 'All Tests'* (or `mvn test`).

For the mobile app, IntelliJ Ultimate handles TypeScript fine, or use VS Code for `mobile/`.

## Try the API (dev mode)

```bash
curl localhost:8080/actuator/health
curl -H "X-Dev-User: dev-ian" localhost:8080/api/me                        # 404 -> needs onboarding
curl -X POST -H "X-Dev-User: dev-ian" -H "Content-Type: application/json" \
     -d '{"handle":"ian","displayName":"Ian"}' localhost:8080/api/me        # 201
curl localhost:8080/api/users/ian                                           # public profile
curl localhost:8080/api/handles/admin/available                            # reserved
```

## Run the mobile app

```bash
cd mobile
cp .env.example .env.local     # on a real phone, set EXPO_PUBLIC_API_URL to your laptop's LAN IP
npm install
npx expo start                 # scan the QR code with Expo Go
```

## API (Phase 0)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/me` | yes | Current profile. `404` means the user still needs to pick a handle |
| POST | `/api/me` | yes | Create public profile (handle, displayName, bio) |
| PATCH | `/api/me` | yes | Edit displayName / bio / avatarUrl |
| DELETE | `/api/me` | yes | In-app account deletion (App Store requirement) |
| GET | `/api/users/{handle}` | no | Public profile |
| GET | `/api/handles/{handle}/available` | no | Live check for onboarding |

## Auth

- **Prod:** Firebase ID tokens, validated as JWTs. Set `FIREBASE_PROJECT_ID`.
- **Local:** profile `local` → send `X-Dev-User: <any-id>`. Never enable `AUTH_DEV_MODE` in production.

## Environment variables (backend)

| Var | Default |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/caraoke` |
| `DATABASE_USER` / `DATABASE_PASSWORD` | `caraoke` |
| `FIREBASE_PROJECT_ID` | `caraoke-dev` |
| `AUTH_DEV_MODE` | `false` |
| `PORT` | `8080` |

## Next (Phase 0 → 1)

- [ ] Create Firebase project; add Firebase Auth to the app (Apple + Google sign-in)
- [ ] `DELETE /me` should also delete the Firebase user (Firebase Admin SDK)
- [ ] Deploy backend to Railway (Dockerfile included) + Railway Postgres
- [ ] Expo Router + onboarding screen (handle picker using `/handles/{h}/available`)
- [ ] Song search (iTunes Search API) → `songs` upsert → song page + stories
