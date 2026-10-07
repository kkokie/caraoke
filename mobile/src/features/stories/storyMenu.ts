import { Alert } from 'react-native';
import { router } from 'expo-router';
import { api, Story } from '@/api/client';

/**
 * The ••• menu on your own story: Edit or Delete.
 * onDeleted lets the calling list drop the story without a refetch.
 */
export function openStoryMenu(story: Story, onDeleted?: (id: number) => void) {
  Alert.alert('Your story', undefined, [
    { text: 'Edit', onPress: () => router.push({ pathname: '/story/[id]/edit', params: { id: String(story.id) } }) },
    { text: 'Delete', style: 'destructive', onPress: () => confirmDelete(story, onDeleted) },
    { text: 'Cancel', style: 'cancel' },
  ]);
}

function confirmDelete(story: Story, onDeleted?: (id: number) => void) {
  Alert.alert('Delete this story?', 'This can’t be undone.', [
    { text: 'Cancel', style: 'cancel' },
    {
      text: 'Delete',
      style: 'destructive',
      onPress: async () => {
        try {
          await api.deleteStory(story.id);
          onDeleted?.(story.id);
        } catch (e) {
          Alert.alert('Couldn’t delete', e instanceof Error ? e.message : String(e));
        }
      },
    },
  ]);
}
