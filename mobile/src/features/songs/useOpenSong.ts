import { useState } from 'react';
import { Alert } from 'react-native';
import { router } from 'expo-router';
import { api, SongSearchResult } from '@/api/client';

/**
 * Tapping a search result: ask the server for OUR song (it creates it on first open),
 * then go to its page, or straight to sharing when search was opened from the + tab.
 * Tracks which row is loading so only that row shows a spinner.
 */
export function useOpenSong(intent: 'browse' | 'share' = 'browse') {
  const [openingId, setOpeningId] = useState<string | null>(null);

  async function open(result: SongSearchResult) {
    if (openingId) return;
    setOpeningId(result.appleId);
    try {
      const song = await api.resolveSong(result.appleId);
      if (intent === 'share') {
        router.replace({ pathname: '/song/[id]/compose', params: { id: String(song.id), from: 'plus' } });
      } else {
        router.push({ pathname: '/song/[id]', params: { id: String(song.id) } });
      }
    } catch (e) {
      Alert.alert('Couldn’t open song', e instanceof Error ? e.message : String(e));
    } finally {
      setOpeningId(null);
    }
  }

  return { open, openingId };
}
