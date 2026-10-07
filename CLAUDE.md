# CLAUDE.md: caraoke

Context for any Claude session (claude.ai chat or Claude Code in IntelliJ). Keep this file current when decisions change.

## Product

A mobile-first social app for sharing the memories and nostalgia songs hold, inspired by how people tell personal stories in YouTube comments.

Core loop (v1):
- **Song page is the "room":** each song has its own feed of stories.
- **Stories can be blog-length** (up to 10,000 chars; enforced in `StoryRules` + a DB check). Feeds show an ~8-line preview with "Read more"; the story page shows it all. (Story Radio will read an excerpt, not a whole essay.)
- **Moments:** a story can pin to a timestamp in the song ("2:14, the bridge…") and a year the memory is from.
- **"I felt this too" (resonance):** the v1 connection mechanic. People connect by recognizing the same stories.

**Following** (followers/following, Follow button) is in. Out of scope for v1: DMs, playlists.

**Public profiles:** every user has a unique public `@handle` to keep the app authentic and discourage bots and spam.

## Platforms

Required targets: **iOS + Android phones** (one Expo codebase; test both), **Apple CarPlay**, and **Android Auto** (plus Android Automotive OS where it comes free).

Car constraints (checked Oct 2026), which shape the design:
- Neither car platform has a "social / text feed" category. CarPlay entitlements: Audio, Communication, EV Charging, Navigation, Parking, Quick Food Ordering. Android Auto: Media (audio), Messaging, Navigation, POI, IoT, Weather (video/games/browsers are parked-only).
- So **in the car, caraoke is an audio experience** (CarPlay *Audio* / Android Auto *Media*): stories are **heard, not read**. Plan: text-to-speech of stories now, and optionally recorded voice stories later, browsed through the cars' built-in list/now-playing templates. No reading or typing while driving; posting from the car, if ever, is voice-only.
- CarPlay needs a paid Apple Developer account plus a **CarPlay Audio entitlement request that Apple reviews** (approval isn't guaranteed). Both car integrations are native code, so they need a **development build, not Expo Go**, wired in through config plugins (never hand-edit `ios/` or `android/`).
- Design for it now: keep stories short and speakable, and plan a per-song "story queue" API the car player can stream through.

## Stack

| Layer | Choice |
|---|---|
| Mobile | Expo (React Native + TypeScript), Expo Router for navigation |
| Backend | Spring Boot 3.5 / Java 21, single deployable (monolith) |
| DB | PostgreSQL, schema owned by Flyway (`ddl-auto: validate`) |
| Auth | Firebase Auth (Apple + Google); backend validates Firebase ID tokens as JWTs |
| Hosting | Railway (Dockerfile in `backend/`) |

**Music data:** do not build on the Spotify Web API. Its Development Mode caps apps at 5 users, and extended quota requires 250k MAU. Use the iTunes Search API / Apple MusicKit for metadata and deep-link out to Spotify, Apple Music, and YouTube. Never host or stream audio.
- Songs are keyed by **our own `songs.id`**; stories reference only that. The iTunes Search API doesn't return ISRCs, so for now we dedupe on `apple_id`; `isrc` stays null until MusicKit.
- All catalog access goes through `CatalogClient` (`com.caraoke.song.catalog`). Swapping providers touches only that package.
- Song metadata is always fetched server-side (`POST /api/songs/resolve` takes only an `appleId`), so clients can't plant fake songs.

## Working agreement

### Git
- **Claude never pushes to `main`.** Claude may push `feat/*` branches for Ian to fetch; Ian reviews, merges, and owns `main`.
- One branch per feature: `feat/<name>`.
- Each feature lands on `main` as **one squashed commit** when merged.
- Pull before starting work, and don't have two sessions editing the same branch.
- **CI** (`.github/workflows/ci.yml`) runs backend `mvn test` and mobile `tsc` on every push. A feature isn't ready to merge until it's green.

### Architecture and code style
- **Monolith deployment, microservice-style code.** Each feature is a self-contained package under `com.caraoke.<feature>` with its own controller, service, repository, entity, and DTOs.
- Features talk to each other through **service classes, never another feature's repository or entity internals**.
- **Small classes, small methods.** No big classes or big methods unless genuinely necessary; split when something grows.
- Cross-cutting code lives in `config/`, `auth/`, `common/`.
- **Dependencies between features point one way, with no cycles.** Example: `story` → `resonance`. The resonance feature only stores and counts; the story feature owns the rules (visible, not your own) and the endpoints.
- **Pages that combine features get their own composition feature on top.** Example: `profile` → `user` + `story` + `follow` (user can't call story, because story already calls user).
  Current graph: `profile → user, story, follow, resonance` · `story → user, song, resonance` · `user, song, resonance, follow → (nothing)`. `media` is infrastructure (like `common/`): any feature may use it, it uses none.
- **Tests:** unit tests (Mockito) for services, `@WebMvcTest` for controllers, and `PersistenceTest` (Testcontainers Postgres) for anything with real SQL: native queries, projections, keyset paging. Booting it also proves Flyway and entity mappings agree.
- Entities reference other features' rows **by id only** (e.g. `Story.userId`, `Story.songId`), no cross-feature JPA relations. Cross-feature reads go through a service method that returns a DTO (e.g. `UserService.findAuthors` → `Author`).
- Feeds use **keyset pagination** (`?before=<id>`, fetch `size+1` to detect more), not offset paging. When ordering isn't by id (e.g. "stories I felt" by resonance time), use an **opaque cursor** carrying the sort key plus a tiebreaker (`FeltCursor` = createdAt + storyId).
- **Privacy defaults:** what you *felt* is private (only `/api/me/felt`, never by handle).

### Conventions
- Schema changes: new Flyway migration `V<n>__<description>.sql`. Never edit an applied migration.
- DTOs are Java records. Never expose `auth_uid` in responses.
- **Media:** the DB stores storage keys (`avatars/<userId>/<uuid>.jpg`), never URLs; DTOs turn keys into URLs via `MediaStorage.publicUrl`. A fresh key per upload (cache forever). Validate uploads by magic bytes (`ImageRules`), never by client content type. Delete old files after commit (`MediaCleanup`). Clients never send a URL for media. Mobile resizes before uploading and loads relative URLs via `mediaUrl()`.
- Handles: lowercase, `[a-z0-9_]{3,20}`, rules centralized in `HandleRules`.
- Local dev: Spring profile `local` enables the `X-Dev-User` header in place of a JWT. Never enable `AUTH_DEV_MODE` in production.
- Mobile: add packages with `npx expo install`; run `npx tsc --noEmit` before committing. See `mobile/AGENTS.md`.
- Mobile layout (same feature-module idea as the backend):
  - `src/app/`: routes only (Expo Router). Screens stay thin and visual.
  - `src/features/<feature>/`: hooks and components for one feature (state + logic live in hooks).
  - `src/components/`: shared UI primitives. `src/api/`: API client. `src/lib/`: pure helpers. `src/theme.ts`: design tokens.
  - Navigation is driven by session state via `Stack.Protected` in `src/app/_layout.tsx`, not by screens pushing each other.
  - Bottom tabs live in `src/app/(tabs)/` (Home, Profile) using `expo-router/js-tabs`; everything else is a stack screen pushed over them.

## Roadmap

Long-term goals: a **sustainable business** (subscription first, partnerships later), **CarPlay / Android Auto "story radio"**, and Android parity. Keep these in mind when making design calls.

| Phase | Theme | Scope | Status |
|---|---|---|---|
| 0 | Foundation | API skeleton, schema, dev auth, Expo shell, CI | done |
| 1 | Core loop | Song search, song page, stories (moment + year), feed, resonance | done |
| **2** | **Profile + safety** | Instagram-style profile (grid; tile = story photo else album cover), view others' profiles, edit/delete stories, edit profile, delete account; photos (`story_media` + object storage); report, block, moderation queue; legal pages | **in progress** |
| 3 | Launch platform | Apple Developer account, EAS dev build, Firebase Auth (Apple + Google), Railway deploy, TestFlight beta, basic product analytics (weekly posters, resonances per story) | |
| 4 | Monetize | RevenueCat + **caraoke+** subscription; **Story Radio** car mode as the flagship premium feature; Apple Music affiliate links | |
| 5 | Growth | Instagram Stories share cards, yearly "your life in songs" recap | |
| 6 | Connection + B2B | "Stories like yours" (pgvector), artist pages, label/artist partnerships and fan insights | |

### Monetization principles
- **Never paywall the network:** posting, reading, and resonating are always free.
- **caraoke+ (subscription, ~$3.99/mo or ~$29.99/yr):** Story Radio car mode, extra photos per story, music memoir/timeline + yearly recap, insights, profile themes. Store fee is 15% under the Apple/Google small-business programs.
- **Apple Music affiliate token** on "Listen on Apple Music" links (Apple Services Performance Partners pays on qualifying Apple Music memberships).
- **Partnerships** (Apple Music, Spotify, labels, artists) once there's traction: our unique asset is *why* people love a song, at which second, and from which year of their life.
- **No ads** early: they need scale and clash with the intimate tone.

### Story Radio (car + premium)
A per-song audio experience: after a song plays, a **host voice reads the stories** people shared about it (TTS first, recorded voice stories later). It is the CarPlay *Audio* / Android Auto *Media* experience and the flagship caraoke+ feature. Design now: stories stay short and speakable; plan a per-song "story queue" API.

### Data-model decisions made ahead of time
- Photos live in **`story_media`** (`story_id`, `position`, `storage_key`, `width`, `height`): many per story, ordered; the DB stores object-storage keys only, never blobs. Profile tile = first photo, else `songs.album_art_url`.
- Stories get **`edited_at`** for edits.
