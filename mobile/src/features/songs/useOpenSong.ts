import { useState } from 'react';
import { Alert } from 'react-native';
import { router } from 'expo-router';
import { api, SongSearchResult } from '@/api/client';

/**
 * Tapping a search result: ask the server for OUR song (it creates it on first open),
 * then go to its page. Tracks which row is loading so only that row shows a spinner.
 */
export function useOpenSong() {
  const [openingId, setOpeningId] = useState<string | null>(null);

  async function open(result: SongSearchResult) {
    if (openingId) return;
    setOpeningId(result.appleId);
    try {
      const song = await api.resolveSong(result.appleId);
      router.push({ pathname: '/song/[id]', params: { id: String(song.id) } });
    } catch (e) {
      Alert.alert('Couldn’t open song', e instanceof Error ? e.message : String(e));
    } finally {
      setOpeningId(null);
    }
  }

  return { open, openingId };
}
