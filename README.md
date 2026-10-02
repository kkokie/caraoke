# Caraoke

Share the memories songs hold. Mobile-first (Expo) + Spring Boot API + PostgreSQL.

```
caraoke/
├── backend/            Spring Boot 3.5 / Java 21 / Flyway / Postgres
├── mobile/             Expo (React Native + TypeScript)
└── docker-compose.yml  Local Postgres
```

## First-time setup

1. **IntelliJ:** File → Open the repo folder. If `backend/` isn't picked up as Maven, right-click `backend/pom.xml` → *Add as Maven Project*.
2. **SDK:** Project Structure → SDK = Java 21 (on Apple Silicon pick the **aarch64** build).
3. **Postgres:** `docker compose up -d` once. It has `restart: unless-stopped`, so it comes back by itself whenever Docker Desktop starts.
4. **App deps:** `cd mobile && npm ci`
5. *(Optional)* IntelliJ → Settings → Build, Execution, Deployment → Compiler → **Build project automatically**, and
   Settings → Advanced Settings → **Allow auto-make to start even if developed application is currently running**.
   With that on, DevTools restarts the API a second after you save a Java file.

## Daily loop

| Step | How | When |
|---|---|---|
| Get changes | `git pull` (or check out the feature branch) | Start of session |
| Backend tests | **Nothing to do:** GitHub Actions runs them on every push (✅/❌ on the branch/PR) | Automatic |
| Run the API | IntelliJ ▶ **Caraoke API (local)** (shared run config: builds first, `local` profile set) | Once; DevTools restarts it on rebuild (⌘F9) |
| Run the app | `cd mobile && npx expo start` → open from Expo Go → Development servers | Once; JS changes hot-reload on their own |
| New mobile packages | `npm ci` then `npx expo start -c` | Only when `package-lock.json` changed |

The app finds the API on its own: it calls port 8080 on the same machine Expo serves from, so a new Wi-Fi or IP needs no config.
Set `EXPO_PUBLIC_API_URL` in `mobile/.env.local` only to point at a deployed API or when using `expo start --tunnel`.

## Try the API (dev mode)

```bash
curl localhost:8080/actuator/health
curl -H "X-Dev-User: dev-ian" localhost:8080/api/me                        # 404 -> needs onboarding
curl -X POST -H "X-Dev-User: dev-ian" -H "Content-Type: application/json" \
     -d '{"handle":"ian","displayName":"Ian"}' localhost:8080/api/me        # 201
curl localhost:8080/api/users/ian                                           # public profile
curl localhost:8080/api/handles/admin/available                            # reserved
```

## API

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/me` | yes | Current profile. `404` means the user still needs to pick a handle |
| POST | `/api/me` | yes | Create public profile (handle, displayName, bio) |
| PATCH | `/api/me` | yes | Edit displayName / bio / avatarUrl |
| DELETE | `/api/me` | yes | In-app account deletion (App Store requirement) |
| GET | `/api/users/{handle}` | no | Public profile |
| GET | `/api/handles/{handle}/available` | no | Live check for onboarding |
| GET | `/api/songs/search?q=` | yes | Search the music catalog (iTunes). Results aren't saved |
| POST | `/api/songs/resolve` | yes | `{appleId}` → our song (created on first open; metadata fetched server-side) |
| GET | `/api/songs/{id}` | yes | Song page data + "listen on" links |

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
