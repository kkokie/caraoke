import { useCallback, useState } from 'react';
import { Alert } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { api, ModerationAction, QueueItem } from '@/api/client';

export type QueueState =
  | { kind: 'loading' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; items: QueueItem[]; nextCursor: number | null; loadingMore: boolean };

const keyOf = (i: QueueItem) => `${i.kind}:${i.story?.id ?? i.person?.handle ?? i.firstReportId}`;

/** The review queue, oldest first. Deciding an item takes it off the list. */
export function useModerationQueue() {
  const [state, setState] = useState<QueueState>({ kind: 'loading' });

  const load = useCallback(async () => {
    try {
      const page = await api.moderationQueue();
      setState({ kind: 'ready', items: page.items, nextCursor: page.nextCursor, loadingMore: false });
    } catch (e) {
      setState({ kind: 'error', message: e instanceof Error ? e.message : 'Couldn’t load the queue' });
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const loadMore = useCallback(async () => {
    if (state.kind !== 'ready' || !state.nextCursor || state.loadingMore) return;
    setState({ ...state, loadingMore: true });
    try {
      const page = await api.moderationQueue(state.nextCursor);
      setState({ kind: 'ready', items: [...state.items, ...page.items], nextCursor: page.nextCursor, loadingMore: false });
    } catch {
      setState({ ...state, loadingMore: false });
    }
  }, [state]);

  const decide = useCallback((item: QueueItem, action: ModerationAction) => {
    const run = async () => {
      try {
        if (item.kind === 'STORY' && item.story) await api.decideStory(item.story.id, action);
        else if (item.person) await api.decidePerson(item.person.handle, action);
        setState((s) => s.kind === 'ready' ? { ...s, items: s.items.filter((i) => keyOf(i) !== keyOf(item)) } : s);
      } catch (e) {
        Alert.alert('Couldn’t save that', e instanceof Error ? e.message : 'Try again');
      }
    };
    if (action === 'DISMISS') return void run();
    Alert.alert(confirmTitle(item, action), confirmBody(action), [
      { text: 'Cancel', style: 'cancel' },
      { text: action === 'BAN' ? 'Ban' : 'Strike', style: 'destructive', onPress: run },
    ]);
  }, []);

  return { state, load, loadMore, decide, keyOf };
}

function confirmTitle(item: QueueItem, action: ModerationAction): string {
  const who = item.person ? `@${item.person.handle}` : 'this person';
  if (action === 'BAN') return `Ban ${who}?`;
  return item.kind === 'STORY' ? `Remove the story and strike ${who}?` : `Strike ${who}?`;
}

function confirmBody(action: ModerationAction): string {
  return action === 'BAN'
    ? 'All their stories come down and they can’t sign back in. A reinstate can undo it.'
    : '1 strike warns, 2 pause sharing for a week, 3 ban.';
}
