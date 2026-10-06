import { useEffect, useState } from 'react';
import { api, Author } from '@/api/client';

export type ResonatorsState =
  | { kind: 'loading' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; people: Author[] };

export function useResonators(storyId: number): ResonatorsState {
  const [state, setState] = useState<ResonatorsState>({ kind: 'loading' });

  useEffect(() => {
    let cancelled = false;
    api.getResonators(storyId)
      .then((people) => !cancelled && setState({ kind: 'ready', people }))
      .catch((e) => !cancelled && setState({ kind: 'error', message: e instanceof Error ? e.message : String(e) }));
    return () => {
      cancelled = true;
    };
  }, [storyId]);

  return state;
}
