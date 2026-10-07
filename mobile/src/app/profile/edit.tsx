import { StyleSheet, Text, View } from 'react-native';
import { PublicProfile } from '@/api/client';
import { Screen } from '@/components/Screen';
import { TextField } from '@/components/TextField';
import { Button } from '@/components/Button';
import { AvatarPicker } from '@/features/profile/AvatarPicker';
import { MAX_BIO, useEditProfile } from '@/features/profile/useEditProfile';
import { useSession } from '@/features/session/useSession';
import { colors, space, type } from '@/theme';

export default function EditProfileScreen() {
  const session = useSession();
  if (session.status !== 'ready') return null;
  return <EditProfileForm profile={session.profile} />;
}

function EditProfileForm({ profile }: { profile: PublicProfile }) {
  const form = useEditProfile(profile);

  return (
    <Screen>
      <AvatarPicker profile={profile} />
      <TextField
        label="Handle"
        prefix="@"
        value={profile.handle}
        editable={false}
        footer={<Text style={[type.hint, styles.muted]}>Handles can’t be changed.</Text>}
      />
      <TextField label="Display name" value={form.displayName} onChangeText={form.setDisplayName} autoComplete="name" />
      <TextField
        label="Bio"
        value={form.bio}
        onChangeText={form.setBio}
        placeholder="The song that raised you…"
        multiline
        style={styles.bio}
        footer={<Text style={[type.hint, styles.counter]}>{form.bio.length}/{MAX_BIO}</Text>}
      />

      {form.error ? <Text style={[type.hint, styles.error]}>{form.error}</Text> : null}
      <Button label="Save" onPress={form.save} disabled={!form.canSave} loading={form.saving} />

      <View style={styles.danger}>
        <Text style={type.label}>ACCOUNT</Text>
        <Button label="Delete account" variant="ghost" onPress={form.confirmDeleteAccount} />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  bio: { minHeight: 88, textAlignVertical: 'top' },
  counter: { color: colors.textMuted, textAlign: 'right' },
  muted: { color: colors.textMuted },
  error: { color: colors.danger, textAlign: 'center' },
  danger: { marginTop: 'auto', gap: space.sm, paddingTop: space.xl },
});
