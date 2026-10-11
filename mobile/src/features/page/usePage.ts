import { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { api, PageView } from '@/api/client';

/**
 * Someone's decorated page, refreshed whenever their profile comes back into view (so your
 * edits show on return). Decoration is optional: if it fails to load, the profile still works.
 */
export function usePage(handle: string) {
  const [page, setPage] = useState<PageView | null>(null);

  const refresh = useCallback(async () => {
    try {
      setPage(await api.getPage(handle));
    } catch {
      setPage(null);
    }
  }, [handle]);

  useFocusEffect(useCallback(() => { refresh(); }, [refresh]));

  return { page, refresh };
}
