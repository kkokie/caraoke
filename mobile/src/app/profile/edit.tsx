import { Text, View } from 'react-native';
import { router } from 'expo-router';
import { PublicProfile } from '@/api/client';
import { Screen } from '@/components/Screen';
import { TextField } from '@/components/TextField';
import { Button } from '@/components/Button';
import { AvatarPicker } from '@/features/profile/AvatarPicker';
import { emailSupport, openLegal } from '@/lib/legal';
import { MAX_BIO, useEditProfile } from '@/features/profile/useEditProfile';
import { useStanding } from '@/features/moderation/useStanding';
import { useSession } from '@/features/session/useSession';
import { makeStyles, space, useTheme } from '@/theme';

export default function EditProfileScreen() {
  const session = useSession();
  if (session.status !== 'ready') return null;
  return <EditProfileForm profile={session.profile} />;
}

function EditProfileForm({ profile }: { profile: PublicProfile }) {
  const styles = useStyles();
  const { type } = useTheme();
  const form = useEditProfile(profile);
  const standing = useStanding();

  return (
    <Screen>
      <AvatarPicker profile={profile} />
      <Button label="Decorate your page" variant="ghost" onPress={() => router.push('/profile/page')} />
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

      <View style={styles.about}>
        <Text style={type.label}>ABOUT</Text>
        <Button label="Community Guidelines" variant="ghost" onPress={() => openLegal('guidelines')} />
        <Button label="Terms of Service" variant="ghost" onPress={() => openLegal('terms')} />
        <Button label="Privacy Policy" variant="ghost" onPress={() => openLegal('privacy')} />
        <Button label="Contact support" variant="ghost" onPress={emailSupport} />
      </View>

      <View style={styles.danger}>
        <Text style={type.label}>ACCOUNT</Text>
        <Button label="Blocked accounts" variant="ghost" onPress={() => router.push('/settings/blocked')} />
        {standing?.admin ? <Button label="Review queue" variant="ghost" onPress={() => router.push('/admin/queue')} /> : null}
        <Button label="Delete account" variant="ghost" onPress={form.confirmDeleteAccount} />
      </View>
    </Screen>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  bio: { minHeight: 88, textAlignVertical: 'top' },
  counter: { color: colors.textMuted, textAlign: 'right' },
  muted: { color: colors.textMuted },
  error: { color: colors.danger, textAlign: 'center' },
  about: { gap: space.sm, paddingTop: space.lg },
  danger: { marginTop: 'auto', gap: space.sm, paddingTop: space.xl },
}));
