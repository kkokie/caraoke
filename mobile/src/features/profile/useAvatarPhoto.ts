import { useState } from 'react';
import { ActionSheetIOS, Alert, Platform } from 'react-native';
import { api, PublicProfile } from '@/api/client';
import { useSession } from '@/features/session/useSession';
import { pickAvatarImage } from './pickAvatarImage';

/** Change / remove your profile photo. Each change saves right away and updates every screen. */
export function useAvatarPhoto(profile: PublicProfile) {
  const session = useSession();
  const [busy, setBusy] = useState(false);
  const [viewing, setViewing] = useState(false);

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
    const view = () => setViewing(true);
    const options = ['View photo', 'Choose new photo', 'Remove photo', 'Cancel'];
    if (Platform.OS === 'ios') {
      ActionSheetIOS.showActionSheetWithOptions(
        { options, destructiveButtonIndex: 2, cancelButtonIndex: 3 },
        (i) => [view, choose, remove][i]?.(),
      );
    } else {
      Alert.alert('Profile photo', undefined, [
        { text: options[0], onPress: view },
        { text: options[1], onPress: choose },
        { text: options[2], style: 'destructive', onPress: remove },
        { text: options[3], style: 'cancel' },
      ]);
    }
  }

  return { busy, openMenu, hasPhoto: !!profile.avatarUrl, viewing, closeViewer: () => setViewing(false) };
}
