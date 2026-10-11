import { Alert } from 'react-native';
import { router } from 'expo-router';
import { api } from '@/api/client';

/**
 * Block, with a plain-words confirmation. A block works both ways and they aren't told.
 * onDone runs after the server confirms (e.g. drop their story from the list, refresh a profile).
 */
export function confirmBlock(handle: string, onDone?: () => void) {
  Alert.alert(
    `Block @${handle}?`,
    'You won’t see each other’s stories or profiles, and any follows between you end. They won’t be told.',
    [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Block',
        style: 'destructive',
        onPress: async () => {
          try {
            await api.block(handle);
            onDone?.();
          } catch (e) {
            Alert.alert('Couldn’t block', e instanceof Error ? e.message : String(e));
          }
        },
      },
    ],
  );
}

export async function unblock(handle: string, onDone?: () => void) {
  try {
    await api.unblock(handle);
    onDone?.();
  } catch (e) {
    Alert.alert('Couldn’t unblock', e instanceof Error ? e.message : String(e));
  }
}

/**
 * Reasons live on their own screen (Android alerts allow only three buttons).
 * `handle` is the person (for a story, its author) so we can offer to block them afterwards.
 */
export function openReport(target: { storyId?: number; handle: string }) {
  const params = target.storyId != null
    ? { kind: 'STORY', storyId: String(target.storyId), handle: target.handle }
    : { kind: 'USER', handle: target.handle };
  router.push({ pathname: '/report', params });
}
