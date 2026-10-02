# CLAUDE.md: caraoke

Context for any Claude session (claude.ai chat or Claude Code in IntelliJ). Keep this file current when decisions change.

## Product

A mobile-first social app for sharing the memories and nostalgia songs hold, inspired by how people tell personal stories in YouTube comments.

Core loop (v1):
- **Song page is the "room":** each song has its own feed of stories.
- **Moments:** a story can pin to a timestamp in the song ("2:14, the bridge…") and a year the memory is from.
- **"I felt this too" (resonance):** the v1 connection mechanic. People connect by recognizing the same stories.

Out of scope for v1: DMs, following, playlists.

**Public profiles:** every user has a unique public `@handle` to keep the app authentic and discourage bots and spam.

## Stack

| Layer | Choice |
|---|---|
| Mobile | Expo (React Native + TypeScript), Expo Router for navigation |
| Backend | Spring Boot 3.5 / Java 21, single deployable (monolith) |
| DB | PostgreSQL, schema owned by Flyway (`ddl-auto: validate`) |
| Auth | Firebase Auth (Apple + Google); backend validates Firebase ID tokens as JWTs |
| Hosting | Railway (Dockerfile in `backend/`) |

**Music data:** do not build on the Spotify Web API. Its Development Mode caps apps at 5 users, and extended quota requires 250k MAU. Use the iTunes Search API / Apple MusicKit for metadata, key songs by ISRC in our own `songs` table, and deep-link out to Spotify, Apple Music, and YouTube. Never host or stream audio.

## Working agreement

### Git
- **Claude never pushes to `main`.** Claude may push `feat/*` branches for Ian to fetch; Ian reviews, merges, and owns `main`.
- One branch per feature: `feat/<name>`.
- Each feature lands on `main` as **one squashed commit** when merged.
- Pull before starting work, and don't have two sessions editing the same branch.

### Architecture and code style
- **Monolith deployment, microservice-style code.** Each feature is a self-contained package under `com.caraoke.<feature>` with its own controller, service, repository, entity, and DTOs.
- Features talk to each other through **service classes, never another feature's repository or entity internals**.
- **Small classes, small methods.** No big classes or big methods unless genuinely necessary; split when something grows.
- Cross-cutting code lives in `config/`, `auth/`, `common/`.

### Conventions
- Schema changes: new Flyway migration `V<n>__<description>.sql`. Never edit an applied migration.
- DTOs are Java records. Never expose `auth_uid` in responses.
- Handles: lowercase, `[a-z0-9_]{3,20}`, rules centralized in `HandleRules`.
- Local dev: Spring profile `local` enables the `X-Dev-User` header in place of a JWT. Never enable `AUTH_DEV_MODE` in production.
- Mobile: add packages with `npx expo install`; run `npx tsc --noEmit` before committing. See `mobile/AGENTS.md`.
- Mobile layout (same feature-module idea as the backend):
  - `src/app/`: routes only (Expo Router). Screens stay thin and visual.
  - `src/features/<feature>/`: hooks and components for one feature (state + logic live in hooks).
  - `src/components/`: shared UI primitives. `src/api/`: API client. `src/lib/`: pure helpers. `src/theme.ts`: design tokens.
  - Navigation is driven by session state via `Stack.Protected` in `src/app/_layout.tsx`, not by screens pushing each other.

## Roadmap

- **Phase 0, Foundation:** API skeleton, schema, dev auth, Expo shell. *(done)* Remaining: Firebase Auth, Railway deploy.
- **Phase 1, Core loop:** song search, song page, post a story (moment + year), per-song feed, resonate.
- **Phase 2, Safety + launch:** report, block, account deletion (API done), moderation queue, legal pages, store submission.
- **Phase 3, Connection:** "stories like yours" via embeddings (pgvector).
- **Phase 4, Growth:** shareable story cards for IG/TikTok.
