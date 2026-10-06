import { useLocalSearchParams } from 'expo-router';
import { LoadingState } from '@/components/LoadingState';
import { StoryForm } from '@/features/stories/StoryForm';
import { useStoryWithSong } from '@/features/stories/useStoryWithSong';

export default function EditStoryScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = useStoryWithSong(Number(id));

  if (state.kind !== 'ready') return <LoadingState error={state.kind === 'error' ? state.message : null} />;
  return <StoryForm song={state.song} existing={state.story} />;
}
