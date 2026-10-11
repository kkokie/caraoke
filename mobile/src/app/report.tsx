import { Pressable, Text, View } from 'react-native';
import { Stack, useLocalSearchParams } from 'expo-router';
import { Button } from '@/components/Button';
import { Screen } from '@/components/Screen';
import { TextField } from '@/components/TextField';
import { REPORT_REASONS } from '@/features/safety/reasons';
import { ReportTarget, useReport } from '@/features/safety/useReport';
import { makeStyles, radius, space } from '@/theme';

/** Report a story or a person. Opened from a story's ••• menu or a profile's ••• menu. */
export default function ReportScreen() {
  const styles = useStyles();
  const params = useLocalSearchParams<{ kind: string; storyId?: string; handle: string }>();
  const target: ReportTarget = params.kind === 'STORY'
    ? { kind: 'STORY', storyId: Number(params.storyId), handle: params.handle }
    : { kind: 'USER', handle: params.handle };
  const form = useReport(target);

  return (
    <Screen>
      <Stack.Screen options={{ title: target.kind === 'STORY' ? 'Report story' : `Report @${target.handle}` }} />
      <Text style={styles.title}>What’s wrong?</Text>
      <Text style={styles.hint}>Reports are private. @{target.handle} won’t know it was you.</Text>

      <View style={styles.reasons}>
        {REPORT_REASONS.map(({ reason, label }) => (
          <Pressable
            key={reason}
            accessibilityRole="radio"
            accessibilityState={{ checked: form.reason === reason }}
            onPress={() => form.setReason(reason)}
            style={[styles.reason, form.reason === reason && styles.reasonOn]}>
            <Text style={styles.reasonText}>{label}</Text>
            <View style={[styles.dot, form.reason === reason && styles.dotOn]} />
          </Pressable>
        ))}
      </View>

      {form.reason === 'SELF_HARM' ? (
        <Text style={styles.urgent}>
          If someone is in immediate danger, call 911. In the US you can call or text 988 to reach the Suicide & Crisis Lifeline.
        </Text>
      ) : null}

      <TextField
        label="Anything else? (optional)"
        value={form.details}
        onChangeText={form.setDetails}
        placeholder="What should we know?"
        multiline
        style={styles.details}
      />

      <Button label="Send report" onPress={form.send} disabled={!form.canSend} loading={form.sending} />
    </Screen>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  title: { ...type.title, fontSize: 32, lineHeight: 36 },
  hint: { ...type.hint },
  reasons: { gap: space.sm },
  reason: {
    minHeight: 48,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: space.md,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  reasonOn: { borderColor: colors.accent },
  reasonText: { ...type.body },
  dot: { width: 18, height: 18, borderRadius: 9, borderWidth: 2, borderColor: colors.border },
  dotOn: { borderColor: colors.accent, backgroundColor: colors.accent },
  urgent: { ...type.body, color: colors.danger },
  details: { minHeight: 88, textAlignVertical: 'top' },
}));
