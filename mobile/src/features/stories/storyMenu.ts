import { Alert } from 'react-native';
import { router } from 'expo-router';
import { api, Story } from '@/api/client';
import { confirmBlock, openReport } from '@/features/safety/blockActions';

/**
 * The ••• menu on a story. Yours: Edit or Delete. Someone else's: Report or Block.
 * onRemoved lets the calling list drop the story (deleted, or its author blocked) without a refetch.
 */
export function openStoryMenu(story: Story, onRemoved?: (id: number) => void) {
  if (story.mine) openOwnMenu(story, onRemoved);
  else openOthersMenu(story, onRemoved);
}

function openOthersMenu(story: Story, onRemoved?: (id: number) => void) {
  const handle = story.author.handle;
  Alert.alert(`@${handle}’s story`, undefined, [
    { text: 'Report story', onPress: () => openReport({ storyId: story.id, handle }) },
    { text: `Block @${handle}`, style: 'destructive', onPress: () => confirmBlock(handle, () => onRemoved?.(story.id)) },
    { text: 'Cancel', style: 'cancel' },
  ]);
}

function openOwnMenu(story: Story, onDeleted?: (id: number) => void) {
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
