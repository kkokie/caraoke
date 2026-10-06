import { useCallback, useRef, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { api, ProfileView, StoryTile } from '@/api/client';

type ProfileState = {
  status: 'loading' | 'ready' | 'error';
  profile: ProfileView | null;
  tiles: StoryTile[];
  nextCursor: number | null;
  loadingMore: boolean;
  error: string | null;
};

const INITIAL: ProfileState = { status: 'loading', profile: null, tiles: [], nextCursor: null, loadingMore: false, error: null };

/**
 * Header + grid for one profile. Reloads on focus, so edits/deletes made on
 * other screens show up when you come back.
 */
export function useProfile(handle: string) {
  const [state, setState] = useState<ProfileState>(INITIAL);
  const loadingMoreRef = useRef(false);

  const refresh = useCallback(async () => {
    try {
      const [profile, page] = await Promise.all([api.getProfile(handle), api.getProfileStories(handle)]);
      setState({ status: 'ready', profile, tiles: page.items, nextCursor: page.nextCursor, loadingMore: false, error: null });
    } catch (e) {
      setState((s) => ({ ...s, status: 'error', error: e instanceof Error ? e.message : String(e) }));
    }
  }, [handle]);

  useFocusEffect(
    useCallback(() => {
      refresh();
    }, [refresh]),
  );

  async function loadMore() {
    if (!state.nextCursor || loadingMoreRef.current) return;
    loadingMoreRef.current = true;
    setState((s) => ({ ...s, loadingMore: true }));
    try {
      const page = await api.getProfileStories(handle, state.nextCursor);
      setState((s) => ({ ...s, tiles: [...s.tiles, ...page.items], nextCursor: page.nextCursor, loadingMore: false }));
    } catch {
      setState((s) => ({ ...s, loadingMore: false }));
    } finally {
      loadingMoreRef.current = false;
    }
  }

  return { ...state, refresh, loadMore };
}
