// Thin fetch wrapper around the Spring Boot API.
// EXPO_PUBLIC_* env vars are inlined at build time by Expo.
import Constants from 'expo-constants';
import { File } from 'expo-file-system';

const API_PORT = 8080;

/**
 * Where the API lives:
 * 1. EXPO_PUBLIC_API_URL if set (staging/prod builds, or a tunnel), else
 * 2. in development, the same machine that's serving the JS bundle. Expo gives us its
 *    LAN address in hostUri ("192.168.1.75:8081"), so a new Wi-Fi/IP needs no config change.
 */
function resolveBaseUrl(): string {
  if (process.env.EXPO_PUBLIC_API_URL) return process.env.EXPO_PUBLIC_API_URL;
  const devHost = Constants.expoConfig?.hostUri?.split(':')[0];
  return devHost ? `http://${devHost}:${API_PORT}` : `http://localhost:${API_PORT}`;
}

const BASE_URL = resolveBaseUrl();

// Until Firebase is wired up, the backend runs in dev mode and trusts this header.
const DEV_USER = process.env.EXPO_PUBLIC_DEV_USER;

export type PublicProfile = {
  handle: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  joinedAt: string;
};

export type HandleAvailability = {
  handle: string;
  available: boolean;
  reason: string | null;
};

export type CreateProfileBody = {
  handle: string;
  displayName: string;
  bio?: string;
  acceptedTerms: boolean;   // Terms + Community Guidelines
  confirmedAge: boolean;    // 13 or older
};

/** A catalog hit from search. Not saved on our side until someone opens it. */
export type SongSearchResult = {
  appleId: string;
  title: string;
  artist: string;
  album: string | null;
  artworkUrl: string | null;
  durationSec: number | null;
};

export type ListenLinks = {
  appleMusic: string | null;
  spotify: string;
  youtubeMusic: string;
};

/** Our song. `id` is what stories will reference. */
export type Song = {
  id: number;
  title: string;
  artist: string;
  album: string | null;
  artworkUrl: string | null;
  durationSec: number | null;
  listen: ListenLinks;
};

export type Author = {
  handle: string;
  displayName: string;
  avatarUrl: string | null;
};

export type Story = {
  id: number;
  songId: number;
  author: Author;
  body: string;
  momentSec: number | null;
  yearOfMemory: number | null;
  lyricQuote: string | null; // the line they felt, typed from memory
  paper: PaperName;          // the paper the story is printed on
  createdAt: string;
  editedAt: string | null;   // null = never edited
  mine: boolean;
  resonanceCount: number;   // how many people "felt this too"
  resonatedByMe: boolean;
  /** Only on your own stories: 'hidden' = in review after reports, 'removed' = taken down. */
  review?: 'hidden' | 'removed' | null;
  /** The story's photo (null = the album art is the cover). Sizes may be null for layout. */
  photo?: StoryPhoto | null;
};

export type StoryPhoto = { url: string; width: number | null; height: number | null };

export type ResonanceSummary = {
  count: number;
  mine: boolean;
};

export type StoryPage = {
  items: Story[];
  nextCursor: number | null;   // pass as `before` for the next page; null = end of feed
};

export type PaperName = 'cream' | 'dusk' | 'sage' | 'ink' | 'rose' | 'tape';

export type PostStoryBody = {
  body: string;
  momentSec?: number;
  yearOfMemory?: number;
  lyricQuote?: string;
  paper?: PaperName;
};

/** A song as Discover shows it next to a story found away from its song page. */
export type SongSummary = { id: number; title: string; artist: string; artworkUrl: string | null };

/** Discover: a story found away from its song page carries its song with it. */
export type FoundStory = { story: Story; song: SongSummary | null };
export type FoundStoryPage = { items: FoundStory[]; nextCursor: number | null };
export type YearCount = { year: number; stories: number };
export type SongWithCount = { song: SongSummary; stories: number };

export type SongPreview = { url: string; attribution: string };

/** A dot on the moments timeline: a 5-second slot starting at `sec`. */
export type MomentCount = { sec: number; stories: number };

/** One story a day: can you share now, and if not, when (ISO time). */
export type ShareQuota = { canShare: boolean; nextShareAt: string | null };

/** One square on a profile grid. coverUrl = album art (later: first photo, falling back to album art). */
export type StoryTile = {
  storyId: number;
  songId: number;
  songTitle: string | null;
  artist: string | null;
  coverUrl: string | null;
  resonanceCount: number;
};

export type StoryTilePage = {
  items: StoryTile[];
  nextCursor: number | null;
};

/** Your private "Felt" grid. nextCursor is opaque. */
export type FeltTilePage = {
  items: StoryTile[];
  nextCursor: string | null;
};

export type ProfileView = {
  user: PublicProfile;
  stats: { stories: number; felt: number };
  social: { followers: number; following: number; followedByMe: boolean };
  me: boolean;
  blockedByMe: boolean;   // you blocked them: show Unblock, no stories
};

// ---- Your page (decoration) ---------------------------------------------------

export type PageThemeName = 'PAPER' | 'MIDNIGHT' | 'MEADOW' | 'SUNSET' | 'OCEAN' | 'ROSE';
export type PageBackgroundName = 'PLAIN' | 'DOTS' | 'LINES' | 'GRID' | 'STARS';
export type PageTypeName = 'CLASSIC' | 'TYPEWRITER' | 'MODERN';
export type PageBlockName = 'PAGE_SONG' | 'PINNED_STORY' | 'LIFE_IN_SONGS' | 'RAISED_ME';

/** Someone's page. `blocks` = shown, in their order; empty blocks are skipped when drawn. */
export type PageView = {
  theme: PageThemeName;
  background: PageBackgroundName;
  type: PageTypeName;
  blocks: PageBlockName[];
  pageSong: Song | null;
  pinnedStory: StoryTile | null;
  lifeInSongs: { year: number; story: StoryTile }[];
  raisedMe: SongSummary[];
};

/** Full replace of your page. Song ids are ours (resolve a search result first). */
export type PageInput = {
  theme: PageThemeName;
  background: PageBackgroundName;
  type: PageTypeName;
  pageSongId: number | null;
  pinnedStoryId: number | null;
  blocks: PageBlockName[];
  raisedSongIds: number[];
};

export type ReportReason =
  | 'SPAM' | 'HARASSMENT' | 'HATE' | 'SEXUAL' | 'VIOLENCE' | 'SELF_HARM' | 'IMPERSONATION' | 'OTHER';

export type ReportBody =
  | { kind: 'STORY'; storyId: number; reason: ReportReason; details?: string }
  | { kind: 'USER'; handle: string; reason: ReportReason; details?: string };

export type Standing = 'GOOD' | 'WARNED' | 'SUSPENDED' | 'BANNED';

/** Private to you. suspendedUntil is set only while SUSPENDED. */
export type AccountStanding = { standing: Standing; suspendedUntil: string | null; admin: boolean };

// ---- Moderation (admins only) ------------------------------------------------

export type ModerationAction = 'DISMISS' | 'STRIKE' | 'BAN';

export type ModeratedPerson = {
  handle: string;
  displayName: string;
  standing: Standing;
  suspendedUntil: string | null;
  activeStrikes: number;
};

export type QueueItem = {
  kind: 'STORY' | 'USER';
  firstReportId: number;
  reporters: number;
  reasons: ReportReason[];
  lastReportedAt: string;
  person: ModeratedPerson | null;
  story: {
    id: number; songTitle: string; songArtist: string; body: string;
    lyricQuote: string | null; status: 'visible' | 'hidden' | 'removed'; createdAt: string;
  } | null;      // null if the author already deleted it
  reports: { reason: ReportReason; details: string | null; createdAt: string }[];
};

export type QueuePage = { items: QueueItem[]; nextCursor: number | null };
export type ModerationOutcome = { person: ModeratedPerson | null; reportsClosed: number };

export type BlockedAccount = { handle: string; displayName: string; avatarUrl: string | null };

export type FollowState = {
  following: boolean;
  followers: number;
};

export type UpdateProfileBody = {
  displayName?: string;
  bio?: string;
};

export class ApiError extends Error {
  constructor(public status: number, message: string, public code?: string) {
    super(message);
  }

  /** Suspended or banned: the server refused this before trying it. */
  get isStanding() {
    return this.code === 'ACCOUNT_STANDING';
  }
}

async function readError(res: Response): Promise<ApiError> {
  // Spring returns { status, error, message, ... }; prefer the human-readable message
  try {
    const body = await res.json();
    return new ApiError(res.status, body.message || body.error || `Request failed (${res.status})`, body.code);
  } catch {
    return new ApiError(res.status, `Request failed (${res.status})`);
  }
}

/** A full URL on our API server (e.g. the public /legal pages). */
export function apiUrl(path: string): string {
  return `${BASE_URL}${path}`;
}

/** Media URLs from the API may be relative ("/media/…" in dev); make them loadable. */
export function mediaUrl(url: string | null | undefined): string | undefined {
  if (!url) return undefined;
  return /^https?:\/\//.test(url) ? url : `${BASE_URL}${url}`;
}

/** A local image file to upload. width/height (px) when known, so others can lay it out before it loads. */
export type LocalImage = { uri: string; name: string; type: string; width?: number; height?: number };

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  // FormData (uploads) must let fetch set its own multipart boundary
  const isForm = typeof FormData !== 'undefined' && init.body instanceof FormData;
  const headers: Record<string, string> = {
    ...(isForm ? {} : { 'Content-Type': 'application/json' }),
    ...(init.headers as Record<string, string>),
  };
  if (DEV_USER) headers['X-Dev-User'] = DEV_USER;
  // Later: headers.Authorization = `Bearer ${await firebaseUser.getIdToken()}`

  const res = await fetch(`${BASE_URL}${path}`, { ...init, headers });
  if (!res.ok) {
    throw await readError(res);
  }
  return (res.status === 204 ? undefined : await res.json()) as T;
}

export const api = {
  me: () => request<PublicProfile>('/api/me'),

  createProfile: (body: CreateProfileBody) =>
    request<PublicProfile>('/api/me', { method: 'POST', body: JSON.stringify(body) }),

  updateProfile: (body: UpdateProfileBody) =>
    request<PublicProfile>('/api/me', { method: 'PATCH', body: JSON.stringify(body) }),

  uploadAvatar: (image: LocalImage) => {
    // Expo's fetch needs a real Blob; RN's old { uri, name, type } parts throw
    // "Unsupported FormDataPart implementation". expo-file-system's File is a Blob.
    const form = new FormData();
    form.append('file', new File(image.uri) as unknown as Blob, image.name);
    return request<PublicProfile>('/api/me/avatar', { method: 'PUT', body: form });
  },

  removeAvatar: () => request<PublicProfile>('/api/me/avatar', { method: 'DELETE' }),

  deleteAccount: () => request<void>('/api/me', { method: 'DELETE' }),

  getProfile: (handle: string) => request<ProfileView>(`/api/profiles/${encodeURIComponent(handle)}`),

  follow: (handle: string) =>
    request<FollowState>(`/api/profiles/${encodeURIComponent(handle)}/follow`, { method: 'PUT' }),

  unfollow: (handle: string) =>
    request<FollowState>(`/api/profiles/${encodeURIComponent(handle)}/follow`, { method: 'DELETE' }),

  getFollowers: (handle: string) => request<Author[]>(`/api/profiles/${encodeURIComponent(handle)}/followers`),

  getFollowing: (handle: string) => request<Author[]>(`/api/profiles/${encodeURIComponent(handle)}/following`),

  getMyFelt: (cursor?: string | null) =>
    request<FeltTilePage>(`/api/me/felt${cursor ? `?cursor=${encodeURIComponent(cursor)}` : ''}`),

  getProfileStories: (handle: string, before?: number | null) =>
    request<StoryTilePage>(`/api/profiles/${encodeURIComponent(handle)}/stories${before ? `?before=${before}` : ''}`),

  handleAvailable: (handle: string, signal?: AbortSignal) =>
    request<HandleAvailability>(`/api/handles/${encodeURIComponent(handle)}/available`, { signal }),

  searchSongs: (query: string, signal?: AbortSignal) =>
    request<SongSearchResult[]>(`/api/songs/search?q=${encodeURIComponent(query)}`, { signal }),

  resolveSong: (appleId: string) =>
    request<Song>('/api/songs/resolve', { method: 'POST', body: JSON.stringify({ appleId }) }),

  getSong: (id: number) => request<Song>(`/api/songs/${id}`),

  /** `moment` = a dot's `sec` on the moments timeline: only stories pinned there. */
  getStories: (songId: number, before?: number | null, moment?: number | null) => {
    const q = [before ? `before=${before}` : '', moment != null ? `moment=${moment}` : ''].filter(Boolean).join('&');
    return request<StoryPage>(`/api/songs/${songId}/stories${q ? `?${q}` : ''}`);
  },

  postStory: (songId: number, body: PostStoryBody) =>
    request<Story>(`/api/songs/${songId}/stories`, { method: 'POST', body: JSON.stringify(body) }),

  getStory: (id: number) => request<Story>(`/api/stories/${id}`),

  getShareQuota: () => request<ShareQuota>('/api/stories/quota'),

  /** Apple's 30-second clip to stream (never download or save); undefined when there isn't one. */
  getPreview: (songId: number) => request<SongPreview | undefined>(`/api/songs/${songId}/preview`),

  getMoments: (songId: number) => request<MomentCount[]>(`/api/songs/${songId}/moments`),

  /** A random good story you haven't seen this session; undefined when there's nothing left (204). */
  dig: (exclude: number[]) =>
    request<FoundStory | undefined>(`/api/discover/dig${exclude.length ? `?exclude=${exclude.join(',')}` : ''}`),

  discoverYears: () => request<YearCount[]>('/api/discover/years'),

  storiesFromYear: (year: number, before?: number | null) =>
    request<FoundStoryPage>(`/api/discover/years/${year}${before ? `?before=${before}` : ''}`),

  songsFullOfStories: () => request<SongWithCount[]>('/api/discover/songs'),

  report: (body: ReportBody) => request<void>('/api/reports', { method: 'POST', body: JSON.stringify(body) }),

  block: (handle: string) => request<void>(`/api/profiles/${encodeURIComponent(handle)}/block`, { method: 'PUT' }),

  unblock: (handle: string) => request<void>(`/api/profiles/${encodeURIComponent(handle)}/block`, { method: 'DELETE' }),

  blockedAccounts: () => request<BlockedAccount[]>('/api/me/blocked'),

  searchPeople: (q: string) => request<Author[]>(`/api/search/people?q=${encodeURIComponent(q)}`),

  /** Full replace: omitted moment/year are cleared. */
  editStory: (id: number, body: PostStoryBody) =>
    request<Story>(`/api/stories/${id}`, { method: 'PUT', body: JSON.stringify(body) }),

  deleteStory: (id: number) => request<void>(`/api/stories/${id}`, { method: 'DELETE' }),

  uploadStoryPhoto: (storyId: number, image: LocalImage) => {
    const form = new FormData();
    form.append('file', new File(image.uri) as unknown as Blob, image.name);
    const size = image.width && image.height ? `?width=${image.width}&height=${image.height}` : '';
    return request<Story>(`/api/stories/${storyId}/photo${size}`, { method: 'PUT', body: form });
  },

  removeStoryPhoto: (storyId: number) => request<Story>(`/api/stories/${storyId}/photo`, { method: 'DELETE' }),

  resonate: (storyId: number) =>
    request<ResonanceSummary>(`/api/stories/${storyId}/resonance`, { method: 'PUT' }),

  unresonate: (storyId: number) =>
    request<ResonanceSummary>(`/api/stories/${storyId}/resonance`, { method: 'DELETE' }),

  getResonators: (storyId: number) => request<Author[]>(`/api/stories/${storyId}/resonators`),

  getPage: (handle: string) => request<PageView>(`/api/profiles/${encodeURIComponent(handle)}/page`),

  savePage: (body: PageInput) => request<PageView>('/api/me/page', { method: 'PUT', body: JSON.stringify(body) }),

  setPageSong: (songId: number | null) =>
    request<PageView>('/api/me/page/song', { method: 'PUT', body: JSON.stringify({ songId }) }),

  myStanding: () => request<AccountStanding>('/api/me/standing'),

  moderationQueue: (after?: number | null) =>
    request<QueuePage>(`/api/admin/reports${after ? `?after=${after}` : ''}`),

  decideStory: (storyId: number, action: ModerationAction, reason?: ReportReason) =>
    request<ModerationOutcome>(`/api/admin/stories/${storyId}/decision`, { method: 'POST', body: JSON.stringify({ action, reason }) }),

  decidePerson: (handle: string, action: ModerationAction, reason?: ReportReason) =>
    request<ModerationOutcome>(`/api/admin/users/${encodeURIComponent(handle)}/decision`, { method: 'POST', body: JSON.stringify({ action, reason }) }),
};
