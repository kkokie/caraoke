import { useState } from 'react';
import { api } from '@/api/client';
import { normalizeHandle } from '@/lib/handle';
import { useSession } from '@/features/session/useSession';
import { useHandleAvailability } from './useHandleAvailability';

const MAX_NAME = 50;
const MAX_BIO = 280;

/** All state + submit logic for the onboarding form, so the screen stays purely visual. */
export function useOnboardingForm() {
  const { completeOnboarding } = useSession();
  const [handle, setHandle] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [bio, setBio] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const handleStatus = useHandleAvailability(handle);
  const canSubmit = handleStatus.kind === 'available' && displayName.trim().length > 0 && !submitting;

  async function submit() {
    if (!canSubmit) return;
    setSubmitting(true);
    setSubmitError(null);
    try {
      const profile = await api.createProfile({
        handle: normalizeHandle(handle),
        displayName: displayName.trim(),
        bio: bio.trim() || undefined,
      });
      completeOnboarding(profile);   // router guard moves us to /home
    } catch (e) {
      // e.g. someone grabbed the handle between the check and submit (409)
      setSubmitError(e instanceof Error ? e.message : 'Something went wrong');
      setSubmitting(false);
    }
  }

  return {
    fields: { handle, displayName, bio },
    setHandle,
    setDisplayName: (v: string) => setDisplayName(v.slice(0, MAX_NAME)),
    setBio: (v: string) => setBio(v.slice(0, MAX_BIO)),
    limits: { MAX_NAME, MAX_BIO },
    handleStatus,
    canSubmit,
    submitting,
    submitError,
    submit,
  };
}
