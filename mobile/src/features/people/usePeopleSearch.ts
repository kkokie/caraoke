import { useEffect, useState } from 'react';
import { api, Author } from '@/api/client';

const DEBOUNCE_MS = 300;
const MIN_CHARS = 2;   // mirrors the server: shorter queries return nobody

/** Debounced people search by @handle or name. Errors just show no people (songs still work). */
export function usePeopleSearch(query: string, enabled = true): Author[] {
  const [people, setPeople] = useState<Author[]>([]);

  useEffect(() => {
    const q = query.trim();
    if (!enabled || q.replace(/^@/, '').length < MIN_CHARS) return setPeople([]);
    let alive = true;
    const timer = setTimeout(() => {
      api.searchPeople(q).then((p) => alive && setPeople(p)).catch(() => alive && setPeople([]));
    }, DEBOUNCE_MS);
    return () => {
      alive = false;
      clearTimeout(timer);
    };
  }, [query, enabled]);

  return people;
}
