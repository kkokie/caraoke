import { useEffect, useState } from 'react';
import { api } from '@/api/client';
import { normalizeHandle, validateHandle } from '@/lib/handle';

export type HandleStatus =
  | { kind: 'empty' }
  | { kind: 'invalid'; reason: string }
  | { kind: 'checking' }
  | { kind: 'available'; handle: string }
  | { kind: 'taken'; reason: string }
  | { kind: 'error' };

const DEBOUNCE_MS = 400;

/**
 * Live handle check: instant local validation, then a debounced server check.
 * Stale requests are aborted so a slow response can't overwrite a newer one.
 */
export function useHandleAvailability(raw: string): HandleStatus {
  const [status, setStatus] = useState<HandleStatus>({ kind: 'empty' });

  useEffect(() => {
    const handle = normalizeHandle(raw);
    if (!handle) return setStatus({ kind: 'empty' });

    const problem = validateHandle(handle);
    if (problem) return setStatus({ kind: 'invalid', reason: problem });

    setStatus({ kind: 'checking' });
    const controller = new AbortController();
    const timer = setTimeout(() => checkServer(handle, controller.signal, setStatus), DEBOUNCE_MS);

    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [raw]);

  return status;
}

async function checkServer(handle: string, signal: AbortSignal, set: (s: HandleStatus) => void) {
  try {
    const res = await api.handleAvailable(handle, signal);
    set(res.available
      ? { kind: 'available', handle: res.handle }
      : { kind: 'taken', reason: res.reason ?? 'That handle is taken' });
  } catch {
    if (!signal.aborted) set({ kind: 'error' });
  }
}
