import { useState } from 'react';
import { Alert, StyleSheet, Text, View } from 'react-native';
import { Screen } from '@/components/Screen';
import { Button } from '@/components/Button';
import { ProfileCard } from '@/features/profile/ProfileCard';
import { useSession } from '@/features/session/useSession';
import { colors, space, type } from '@/theme';

// Placeholder home until Phase 1 (song search + feeds) lands.
export default function HomeScreen() {
  const session = useSession();
  if (session.status !== 'ready') return null;   // guard in _layout redirects; this just narrows the type

  return (
    <Screen>
      <View style={styles.header}>
        <Text style={type.title}>You’re in.</Text>
        <Text style={type.subtitle}>Song pages and stories are coming next.</Text>
      </View>

      <ProfileCard profile={session.profile} />

      {__DEV__ ? <DevReset onReset={session.resetProfile} /> : null}
    </Screen>
  );
}

/** Dev-only: deletes this profile so you can walk through onboarding again. */
function DevReset({ onReset }: { onReset: () => Promise<void> }) {
  const [busy, setBusy] = useState(false);

  const confirm = () =>
    Alert.alert('Reset profile?', 'Deletes this dev profile so you can redo onboarding.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Reset', style: 'destructive', onPress: run },
    ]);

  async function run() {
    setBusy(true);
    try {
      await onReset();
    } catch (e) {
      setBusy(false);
      Alert.alert('Reset failed', e instanceof Error ? e.message : String(e));
    }
  }

  return (
    <View style={styles.dev}>
      <Text style={[type.hint, styles.devLabel]}>DEV TOOLS</Text>
      <Button label="Reset profile and redo onboarding" variant="ghost" onPress={confirm} loading={busy} />
    </View>
  );
}

const styles = StyleSheet.create({
  header: { gap: space.sm, marginTop: space.xl },
  dev: { marginTop: 'auto', gap: space.sm },
  devLabel: { color: colors.textMuted, textAlign: 'center' },
});
