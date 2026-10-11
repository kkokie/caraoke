import { useCallback, useEffect, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { setAudioModeAsync, useAudioPlayer, useAudioPlayerStatus } from 'expo-audio';
import { api, SongPreview } from '@/api/client';

/**
 * Apple's 30-second preview for a song. Streamed straight from Apple (never downloaded or saved),
 * paused when you leave the screen, and rewound when it ends.
 */
export function usePreview(songId: number) {
  const [preview, setPreview] = useState<SongPreview | null>(null);
  const player = useAudioPlayer(preview ? { uri: preview.url } : null, { updateInterval: 250 });
  const status = useAudioPlayerStatus(player);

  useEffect(() => {
    let alive = true;
    api.getPreview(songId).then((p) => alive && setPreview(p ?? null)).catch(() => {});
    setAudioModeAsync({ playsInSilentMode: true }).catch(() => {});   // the ringer switch shouldn't mute a tap
    return () => { alive = false; };
  }, [songId]);

  // Leaving the song page (sharing, a profile, back) stops the music
  useFocusEffect(useCallback(() => () => {
    try {
      player.pause();
    } catch {
      // already released on unmount: nothing is playing
    }
  }, [player]));

  useEffect(() => {
    if (status.didJustFinish) {
      player.pause();
      player.seekTo(0);
    }
  }, [status.didJustFinish, player]);

  const toggle = () => (status.playing ? player.pause() : player.play());

  return {
    available: !!preview,
    attribution: preview?.attribution ?? '',
    playing: status.playing,
    loading: !!preview && !status.isLoaded,
    position: status.currentTime,
    duration: status.duration || 30,
    toggle,
  };
}
