import { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { api, SongWithCount, YearCount } from '@/api/client';

export type Shelves = { years: YearCount[]; songs: SongWithCount[]; loaded: boolean };

/** The two browse shelves on Discover: years that have stories, and songs full of stories. */
export function useShelves(): Shelves {
  const [shelves, setShelves] = useState<Shelves>({ years: [], songs: [], loaded: false });

  useFocusEffect(
    useCallback(() => {
      let alive = true;
      Promise.all([api.discoverYears(), api.songsFullOfStories()])
        .then(([years, songs]) => alive && setShelves({ years, songs, loaded: true }))
        .catch(() => alive && setShelves((s) => ({ ...s, loaded: true })));   // shelves are optional extras
      return () => { alive = false; };
    }, []),
  );

  return shelves;
}
