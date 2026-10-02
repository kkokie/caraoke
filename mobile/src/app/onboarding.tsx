import { StyleSheet, Text, View } from 'react-native';
import { Screen } from '@/components/Screen';
import { TextField } from '@/components/TextField';
import { Button } from '@/components/Button';
import { HandleStatusLine } from '@/features/onboarding/HandleStatusLine';
import { useOnboardingForm } from '@/features/onboarding/useOnboardingForm';
import { colors, space, type } from '@/theme';

export default function OnboardingScreen() {
  const form = useOnboardingForm();

  return (
    <Screen>
      <View style={styles.header}>
        <Text style={type.title}>Every song{'\n'}holds a story.</Text>
        <Text style={type.subtitle}>Pick a handle so people know whose memory they’re reading.</Text>
      </View>

      <TextField
        label="Handle"
        prefix="@"
        value={form.fields.handle}
        onChangeText={form.setHandle}
        placeholder="yourname"
        autoCapitalize="none"
        autoCorrect={false}
        autoComplete="username"
        maxLength={21}
        footer={<HandleStatusLine status={form.handleStatus} />}
      />

      <TextField
        label="Display name"
        value={form.fields.displayName}
        onChangeText={form.setDisplayName}
        placeholder="What should we call you?"
        autoComplete="name"
      />

      <TextField
        label="Bio (optional)"
        value={form.fields.bio}
        onChangeText={form.setBio}
        placeholder="The song that raised you…"
        multiline
        style={styles.bio}
        footer={<Text style={[type.hint, styles.counter]}>{form.fields.bio.length}/{form.limits.MAX_BIO}</Text>}
      />

      <View style={styles.footer}>
        {form.submitError ? <Text style={[type.hint, styles.error]}>{form.submitError}</Text> : null}
        <Button label="Start sharing" onPress={form.submit} disabled={!form.canSubmit} loading={form.submitting} />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  header: { gap: space.sm, marginTop: space.xl },
  bio: { minHeight: 88, textAlignVertical: 'top' },
  counter: { color: colors.textMuted, textAlign: 'right' },
  footer: { marginTop: 'auto', gap: space.sm },
  error: { color: colors.danger, textAlign: 'center' },
});
