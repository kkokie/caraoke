import { useState } from 'react';
import { ActionSheetIOS, Alert, Platform } from 'react-native';
import { api, PublicProfile } from '@/api/client';
import { useSession } from '@/features/session/useSession';
import { pickAvatarImage } from './pickAvatarImage';

/** Change / remove your profile photo. Each change saves right away and updates every screen. */
export function useAvatarPhoto(profile: PublicProfile) {
  const session = useSession();
  const [busy, setBusy] = useState(false);

  async function run(action: () => Promise<PublicProfile | null>) {
    setBusy(true);
    try {
      const updated = await action();
      if (updated) session.updateProfile(updated);
    } catch (e) {
      Alert.alert('Couldn’t update photo', e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  }

  const choose = () =>
    run(async () => {
      const image = await pickAvatarImage();
      return image ? api.uploadAvatar(image) : null;
    });

  const remove = () => run(() => api.removeAvatar());

  function openMenu() {
    if (busy) return;
    if (!profile.avatarUrl) return choose();
    const options = ['Choose new photo', 'Remove photo', 'Cancel'];
    if (Platform.OS === 'ios') {
      ActionSheetIOS.showActionSheetWithOptions(
        { options, destructiveButtonIndex: 1, cancelButtonIndex: 2 },
        (i) => (i === 0 ? choose() : i === 1 ? remove() : undefined),
      );
    } else {
      Alert.alert('Profile photo', undefined, [
        { text: options[0], onPress: choose },
        { text: options[1], style: 'destructive', onPress: remove },
        { text: options[2], style: 'cancel' },
      ]);
    }
  }

  return { busy, openMenu, hasPhoto: !!profile.avatarUrl };
}
