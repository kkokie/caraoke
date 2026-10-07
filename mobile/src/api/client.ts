// Thin fetch wrapper around the Spring Boot API.
// EXPO_PUBLIC_* env vars are inlined at build time by Expo.
import Constants from 'expo-constants';

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
  createdAt: string;
  editedAt: string | null;   // null = never edited
  mine: boolean;
  resonanceCount: number;   // how many people "felt this too"
  resonatedByMe: boolean;
};

export type ResonanceSummary = {
  count: number;
  mine: boolean;
};

export type StoryPage = {
  items: Story[];
  nextCursor: number | null;   // pass as `before` for the next page; null = end of feed
};

export type PostStoryBody = {
  body: string;
  momentSec?: number;
  yearOfMemory?: number;
};

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
};

export type FollowState = {
  following: boolean;
  followers: number;
};

export type UpdateProfileBody = {
  displayName?: string;
  bio?: string;
};

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

async function readErrorMessage(res: Response): Promise<string> {
  // Spring returns { status, error, message, ... }; prefer the human-readable message
  try {
    const body = await res.json();
    return body.message || body.error || `Request failed (${res.status})`;
  } catch {
    return `Request failed (${res.status})`;
  }
}

/** Media URLs from the API may be relative ("/media/…" in dev); make them loadable. */
export function mediaUrl(url: string | null | undefined): string | undefined {
  if (!url) return undefined;
  return /^https?:\/\//.test(url) ? url : `${BASE_URL}${url}`;
}

/** A local image file to upload (React Native's FormData accepts this shape). */
export type LocalImage = { uri: string; name: string; type: string };

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
    throw new ApiError(res.status, await readErrorMessage(res));
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
    const form = new FormData();
    form.append('file', image as unknown as Blob);
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

  getStories: (songId: number, before?: number | null) =>
    request<StoryPage>(`/api/songs/${songId}/stories${before ? `?before=${before}` : ''}`),

  postStory: (songId: number, body: PostStoryBody) =>
    request<Story>(`/api/songs/${songId}/stories`, { method: 'POST', body: JSON.stringify(body) }),

  getStory: (id: number) => request<Story>(`/api/stories/${id}`),

  /** Full replace: omitted moment/year are cleared. */
  editStory: (id: number, body: PostStoryBody) =>
    request<Story>(`/api/stories/${id}`, { method: 'PUT', body: JSON.stringify(body) }),

  deleteStory: (id: number) => request<void>(`/api/stories/${id}`, { method: 'DELETE' }),

  resonate: (storyId: number) =>
    request<ResonanceSummary>(`/api/stories/${storyId}/resonance`, { method: 'PUT' }),

  unresonate: (storyId: number) =>
    request<ResonanceSummary>(`/api/stories/${storyId}/resonance`, { method: 'DELETE' }),

  getResonators: (storyId: number) => request<Author[]>(`/api/stories/${storyId}/resonators`),
};
