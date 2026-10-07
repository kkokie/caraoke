import { useLocalSearchParams } from 'expo-router';
import { LoadingState } from '@/components/LoadingState';
import { ShareFlow } from '@/features/share/ShareFlow';
import { useSong } from '@/features/songs/useSong';

/** Share a story about this song. `from=plus` when it was started from the + tab. */
export default function ComposeScreen() {
  const { id, from } = useLocalSearchParams<{ id: string; from?: string }>();
  const state = useSong(Number(id));

  if (state.kind !== 'ready') return <LoadingState error={state.kind === 'error' ? state.message : null} />;
  return <ShareFlow song={state.song} origin={from === 'plus' ? 'plus' : 'song'} />;
}
