import { Alert } from 'react-native';
import { api, Story } from '@/api/client';

/** Confirm, delete on the server, then drop it from the list without a refetch. */
export function confirmDeleteStory(story: Story, onDeleted: (id: number) => void) {
  Alert.alert('Delete this story?', 'This can’t be undone.', [
    { text: 'Cancel', style: 'cancel' },
    {
      text: 'Delete',
      style: 'destructive',
      onPress: async () => {
        try {
          await api.deleteStory(story.id);
          onDeleted(story.id);
        } catch (e) {
          Alert.alert('Couldn’t delete', e instanceof Error ? e.message : String(e));
        }
      },
    },
  ]);
}
