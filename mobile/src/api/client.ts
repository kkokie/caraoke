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

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
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

  deleteAccount: () => request<void>('/api/me', { method: 'DELETE' }),

  handleAvailable: (handle: string, signal?: AbortSignal) =>
    request<HandleAvailability>(`/api/handles/${encodeURIComponent(handle)}/available`, { signal }),

  searchSongs: (query: string, signal?: AbortSignal) =>
    request<SongSearchResult[]>(`/api/songs/search?q=${encodeURIComponent(query)}`, { signal }),

  resolveSong: (appleId: string) =>
    request<Song>('/api/songs/resolve', { method: 'POST', body: JSON.stringify({ appleId }) }),

  getSong: (id: number) => request<Song>(`/api/songs/${id}`),
};
