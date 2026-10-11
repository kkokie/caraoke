import { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { AccountStanding, api } from '@/api/client';

/** Your standing (and whether you moderate), refreshed whenever the screen comes back into view. */
export function useStanding() {
  const [standing, setStanding] = useState<AccountStanding | null>(null);

  useFocusEffect(
    useCallback(() => {
      let live = true;
      api.myStanding().then((s) => { if (live) setStanding(s); }).catch(() => { /* notice is optional */ });
      return () => { live = false; };
    }, []),
  );

  return standing;
}
