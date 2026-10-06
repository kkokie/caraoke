import { useCallback, useEffect, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { api, Song, Story } from '@/api/client';

export type StoryWithSong =
  | { kind: 'loading' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; story: Story; song: Song };

/** A story plus its song, for the story page and the edit screen. Refreshes on focus. */
export function useStoryWithSong(storyId: number) {
  const [state, setState] = useState<StoryWithSong>({ kind: 'loading' });

  const load = useCallback(async () => {
    try {
      const story = await api.getStory(storyId);
      const song = await api.getSong(story.songId);
      setState({ kind: 'ready', story, song });
    } catch (e) {
      setState({ kind: 'error', message: e instanceof Error ? e.message : String(e) });
    }
  }, [storyId]);

  useEffect(() => {
    setState({ kind: 'loading' });
  }, [storyId]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load]),
  );

  return state;
}
