import { useEffect, useState } from 'react';
import { api, Song } from '@/api/client';

export type SongState =
  | { kind: 'loading' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; song: Song };

export function useSong(id: number): SongState {
  const [state, setState] = useState<SongState>({ kind: 'loading' });

  useEffect(() => {
    let cancelled = false;
    setState({ kind: 'loading' });
    api.getSong(id)
      .then((song) => !cancelled && setState({ kind: 'ready', song }))
      .catch((e) => !cancelled && setState({ kind: 'error', message: e instanceof Error ? e.message : String(e) }));
    return () => {
      cancelled = true;
    };
  }, [id]);

  return state;
}
