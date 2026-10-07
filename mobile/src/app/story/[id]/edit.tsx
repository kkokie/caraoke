import { useLocalSearchParams } from 'expo-router';
import { LoadingState } from '@/components/LoadingState';
import { ShareFlow } from '@/features/share/ShareFlow';
import { useStoryWithSong } from '@/features/stories/useStoryWithSong';

/** Edit a story with the same three steps you shared it with. */
export default function EditStoryScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = useStoryWithSong(Number(id));

  if (state.kind !== 'ready') return <LoadingState error={state.kind === 'error' ? state.message : null} />;
  return <ShareFlow song={state.song} existing={state.story} />;
}
