import { createContext, ReactNode, useCallback, useEffect, useMemo, useState } from 'react';
import { api, ApiError, PublicProfile } from '@/api/client';

// The app is always in exactly one of these states. The router guards on `status`.
export type SessionState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'needsProfile' }
  | { status: 'ready'; profile: PublicProfile };

export type Session = SessionState & {
  reload: () => Promise<void>;
  completeOnboarding: (profile: PublicProfile) => void;
  /** After editing your profile, so every screen shows the new name/bio. */
  updateProfile: (profile: PublicProfile) => void;
  /** Deletes the account (App Store requirement); the guard sends you back to onboarding. */
  deleteAccount: () => Promise<void>;
};

export const SessionContext = createContext<Session | null>(null);

async function loadSession(): Promise<SessionState> {
  try {
    return { status: 'ready', profile: await api.me() };
  } catch (e) {
    // 404 from /api/me = signed in, but no handle yet -> onboarding
    if (e instanceof ApiError && e.status === 404) return { status: 'needsProfile' };
    return { status: 'error', message: e instanceof Error ? e.message : String(e) };
  }
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<SessionState>({ status: 'loading' });

  const reload = useCallback(async () => {
    setState({ status: 'loading' });
    setState(await loadSession());
  }, []);

  const completeOnboarding = useCallback((profile: PublicProfile) => {
    setState({ status: 'ready', profile });
  }, []);

  const updateProfile = useCallback((profile: PublicProfile) => {
    setState({ status: 'ready', profile });
  }, []);

  const deleteAccount = useCallback(async () => {
    await api.deleteAccount();
    setState({ status: 'needsProfile' });
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  const value = useMemo(
    () => ({ ...state, reload, completeOnboarding, updateProfile, deleteAccount }),
    [state, reload, completeOnboarding, updateProfile, deleteAccount],
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}
