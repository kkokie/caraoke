import { useEffect, useState } from 'react';
import { Author } from '@/api/client';

export type PeopleState =
  | { kind: 'loading' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; people: Author[] };

/** Loads any list of people (followers, following, who felt a story). */
export function usePeople(load: () => Promise<Author[]>, key: string): PeopleState {
  const [state, setState] = useState<PeopleState>({ kind: 'loading' });

  useEffect(() => {
    let cancelled = false;
    setState({ kind: 'loading' });
    load()
      .then((people) => !cancelled && setState({ kind: 'ready', people }))
      .catch((e) => !cancelled && setState({ kind: 'error', message: e instanceof Error ? e.message : String(e) }));
    return () => {
      cancelled = true;
    };
    // `key` identifies the list; `load` is a fresh closure every render
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  return state;
}
