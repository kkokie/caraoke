import { useEffect, useState } from 'react';
import { api, SongSearchResult } from '@/api/client';

export type SongSearchState =
  | { kind: 'idle' }
  | { kind: 'searching' }
  | { kind: 'results'; items: SongSearchResult[] }
  | { kind: 'error'; message: string };

const DEBOUNCE_MS = 400;   // also keeps us well under iTunes' ~20 req/min limit
const MIN_CHARS = 2;

/** Debounced, abortable song search. Typing fast never shows stale results. */
export function useSongSearch(query: string): SongSearchState {
  const [state, setState] = useState<SongSearchState>({ kind: 'idle' });

  useEffect(() => {
    const q = query.trim();
    if (q.length < MIN_CHARS) return setState({ kind: 'idle' });

    setState({ kind: 'searching' });
    const controller = new AbortController();
    const timer = setTimeout(() => runSearch(q, controller.signal, setState), DEBOUNCE_MS);

    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [query]);

  return state;
}

async function runSearch(q: string, signal: AbortSignal, set: (s: SongSearchState) => void) {
  try {
    set({ kind: 'results', items: await api.searchSongs(q, signal) });
  } catch (e) {
    if (!signal.aborted) set({ kind: 'error', message: e instanceof Error ? e.message : 'Search failed' });
  }
}
