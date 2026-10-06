import { useState } from 'react';
import { Alert } from 'react-native';
import { router } from 'expo-router';
import { api, PublicProfile } from '@/api/client';
import { useSession } from '@/features/session/useSession';

export const MAX_NAME = 50;
export const MAX_BIO = 280;

/** Edit display name + bio, and the account-deletion flow (App Store requirement). */
export function useEditProfile(current: PublicProfile) {
  const session = useSession();
  const [displayName, setDisplayName] = useState(current.displayName);
  const [bio, setBio] = useState(current.bio ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const changed = displayName.trim() !== current.displayName || bio.trim() !== (current.bio ?? '');
  const canSave = displayName.trim().length > 0 && changed && !saving;

  async function save() {
    if (!canSave) return;
    setSaving(true);
    setError(null);
    try {
      // An empty bio string clears it on the server
      const updated = await api.updateProfile({ displayName: displayName.trim(), bio: bio.trim() });
      session.updateProfile(updated);
      router.back();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Couldn’t save');
      setSaving(false);
    }
  }

  function confirmDeleteAccount() {
    Alert.alert(
      'Delete your account?',
      'This permanently deletes your profile, every story you wrote, and everything you felt. It can’t be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Delete account', style: 'destructive', onPress: deleteAccount },
      ],
    );
  }

  async function deleteAccount() {
    try {
      await session.deleteAccount();   // the session guard takes you to onboarding
    } catch (e) {
      Alert.alert('Couldn’t delete account', e instanceof Error ? e.message : String(e));
    }
  }

  return {
    displayName, setDisplayName: (v: string) => setDisplayName(v.slice(0, MAX_NAME)),
    bio, setBio: (v: string) => setBio(v.slice(0, MAX_BIO)),
    canSave, saving, error, save, confirmDeleteAccount,
  };
}
