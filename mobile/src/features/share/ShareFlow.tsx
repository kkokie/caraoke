import { useEffect, useRef } from 'react';
import { BackHandler, KeyboardAvoidingView, Platform, Pressable, ScrollView, Text, View } from 'react-native';
import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Song, Story } from '@/api/client';
import { Button } from '@/components/Button';
import { BackIcon } from '@/components/icons';
import { LoadingState } from '@/components/LoadingState';
import { makeStyles, space, useTheme } from '@/theme';
import { LookStep } from './LookStep';
import { MomentStep } from './MomentStep';
import { timeUntil, useShareQuota } from './useShareQuota';
import { ShareOrigin, STEPS, useShareDraft } from './useShareDraft';
import { WordsStep } from './WordsStep';

type Props = { song: Song; existing?: Story; origin?: ShareOrigin };

/**
 * The share flow: 1 moment + year → 2 the line + your story → 3 how it looks.
 * One screen with steps (so the draft survives going back and forth); back goes one step at a time.
 */
export function ShareFlow({ song, existing, origin }: Props) {
  const quota = useShareQuota(!existing);
  if (quota.kind === 'checking') return <LoadingState error={null} />;
  if (quota.kind === 'used') return <ComeBackTomorrow nextShareAt={quota.nextShareAt} />;
  return <Steps song={song} existing={existing} origin={origin} />;
}

function Steps({ song, existing, origin }: Props) {
  const styles = useStyles();
  const draft = useShareDraft(song, existing, origin);
  const last = draft.index === STEPS.length - 1;

  // Android's back button steps back too, instead of throwing the draft away
  const back = useRef(draft.back);
  back.current = draft.back;
  useEffect(() => {
    const sub = BackHandler.addEventListener('hardwareBackPress', () => {
      back.current();
      return true;
    });
    return () => sub.remove();
  }, []);

  return (
    <SafeAreaView style={styles.safe} edges={['top', 'bottom', 'left', 'right']}>
      <TopBar onBack={draft.back} label={`${draft.index + 1} OF ${STEPS.length}`} />
      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          {draft.step === 'moment' ? <MomentStep draft={draft} /> : null}
          {draft.step === 'words' ? <WordsStep draft={draft} /> : null}
          {draft.step === 'look' ? <LookStep draft={draft} /> : null}
        </ScrollView>
        <View style={styles.footer}>
          {draft.error ? <Text style={styles.error}>{draft.error}</Text> : null}
          {last
            ? <Button label={draft.isEdit ? 'Save changes' : 'Share story'} onPress={draft.submit} loading={draft.submitting} />
            : <Button label="Next" onPress={draft.next} disabled={!draft.canContinue} />}
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function TopBar({ onBack, label }: { onBack: () => void; label: string }) {
  const styles = useStyles();
  const { colors } = useTheme();
  return (
    <View style={styles.top}>
      <Pressable accessibilityRole="button" accessibilityLabel="Back" onPress={onBack} hitSlop={12} style={styles.back}>
        <BackIcon color={colors.accent} />
      </Pressable>
      <Text style={styles.step}>{label}</Text>
      <View style={styles.back} />
    </View>
  );
}

/** One story a day. Said kindly, with when the next one unlocks. */
function ComeBackTomorrow({ nextShareAt }: { nextShareAt: Date }) {
  const styles = useStyles();
  return (
    <SafeAreaView style={styles.safe}>
      <TopBar onBack={() => router.back()} label="" />
      <View style={styles.rest}>
        <Text style={styles.restTitle}>You’ve shared today’s story.</Text>
        <Text style={styles.restBody}>
          One story a day keeps caraoke honest. Your next one unlocks {timeUntil(nextShareAt)}. Until then, go digging.
        </Text>
        <Button label="Back" variant="ghost" onPress={() => router.back()} />
      </View>
    </SafeAreaView>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  safe: { flex: 1, backgroundColor: colors.bg },
  flex: { flex: 1 },
  top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: space.md, height: 48 },
  back: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center' },
  step: { ...type.label },
  content: { padding: space.lg, paddingTop: space.md, gap: space.lg },
  footer: { paddingHorizontal: space.lg, paddingBottom: space.md, gap: space.sm },
  error: { ...type.hint, color: colors.danger, textAlign: 'center' },
  rest: { flex: 1, padding: space.lg, gap: space.lg, justifyContent: 'center' },
  restTitle: { ...type.title },
  restBody: { ...type.story, color: colors.textMuted },
}));
