import { useEffect, useState } from 'react';
import { api } from '@/api/client';

export type QuotaState =
  | { kind: 'checking' }
  | { kind: 'open' }
  | { kind: 'used'; nextShareAt: Date };

/**
 * One story a day: checked when the composer opens so nobody writes a whole story only to be told
 * "tomorrow". Edits skip the check. If the check itself fails, let them write; the server still decides.
 */
export function useShareQuota(enabled: boolean): QuotaState {
  const [state, setState] = useState<QuotaState>(enabled ? { kind: 'checking' } : { kind: 'open' });

  useEffect(() => {
    if (!enabled) return;
    let alive = true;
    api.getShareQuota()
      .then((q) => {
        if (!alive) return;
        setState(q.canShare || !q.nextShareAt ? { kind: 'open' } : { kind: 'used', nextShareAt: new Date(q.nextShareAt) });
      })
      .catch(() => alive && setState({ kind: 'open' }));
    return () => { alive = false; };
  }, [enabled]);

  return state;
}

/** "in 6h 12m" / "in 45m" / "in a moment" */
export function timeUntil(when: Date, now: Date = new Date()): string {
  const min = Math.ceil((when.getTime() - now.getTime()) / 60000);
  if (min <= 1) return 'in a moment';
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `in ${h}h ${m}m` : `in ${m}m`;
}
