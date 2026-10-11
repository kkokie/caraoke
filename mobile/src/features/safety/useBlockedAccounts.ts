import { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { api, BlockedAccount } from '@/api/client';
import { unblock } from './blockActions';

export type BlockedState = { kind: 'loading' } | { kind: 'ready'; accounts: BlockedAccount[] } | { kind: 'error'; message: string };

/** "Blocked accounts" in settings: who you blocked, and unblocking them. */
export function useBlockedAccounts() {
  const [state, setState] = useState<BlockedState>({ kind: 'loading' });

  const load = useCallback(async () => {
    try {
      setState({ kind: 'ready', accounts: await api.blockedAccounts() });
    } catch (e) {
      setState({ kind: 'error', message: e instanceof Error ? e.message : 'Couldn’t load' });
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const unblockOne = (handle: string) =>
    unblock(handle, () => setState((s) => s.kind === 'ready'
      ? { kind: 'ready', accounts: s.accounts.filter((a) => a.handle !== handle) }
      : s));

  return { state, unblockOne };
}
