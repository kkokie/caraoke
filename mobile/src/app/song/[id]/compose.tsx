import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { Song } from '@/api/client';
import { Screen } from '@/components/Screen';
import { TextField } from '@/components/TextField';
import { Button } from '@/components/Button';
import { Artwork } from '@/features/songs/Artwork';
import { useSong } from '@/features/songs/useSong';
import { MAX_BODY, useComposeStory } from '@/features/stories/useComposeStory';
import { colors, space, type } from '@/theme';

export default function ComposeScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = useSong(Number(id));

  if (state.kind !== 'ready') {
    return (
      <View style={styles.centered}>
        {state.kind === 'loading'
          ? <ActivityIndicator color={colors.accent} />
          : <Text style={[type.subtitle, styles.error]}>{state.message}</Text>}
      </View>
    );
  }
  return <Composer song={state.song} />;
}

function Composer({ song }: { song: Song }) {
  const form = useComposeStory(song);

  return (
    <Screen>
      <View style={styles.songRow}>
        <Artwork uri={song.artworkUrl} size={44} />
        <View style={styles.songText}>
          <Text style={styles.songTitle} numberOfLines={1}>{song.title}</Text>
          <Text style={styles.songArtist} numberOfLines={1}>{song.artist}</Text>
        </View>
      </View>

      <TextField
        label="Your story"
        value={form.body}
        onChangeText={form.setBody}
        placeholder="Where were you the first time you heard it? Who does it remind you of?"
        multiline
        autoFocus
        style={styles.body}
        footer={<Text style={[type.hint, styles.counter]}>{form.body.length}/{MAX_BODY}</Text>}
      />

      <View style={styles.row}>
        <View style={styles.half}>
          <TextField
            label="Moment (optional)"
            value={form.momentText}
            onChangeText={form.setMomentText}
            placeholder="2:14"
            keyboardType="numbers-and-punctuation"
            maxLength={5}
            footer={<FieldHint error={form.momentError} hint="The part that gets you" />}
          />
        </View>
        <View style={styles.half}>
          <TextField
            label="Year (optional)"
            value={form.yearText}
            onChangeText={form.setYearText}
            placeholder="2009"
            keyboardType="number-pad"
            maxLength={4}
            footer={<FieldHint error={form.yearError} hint="When this memory is from" />}
          />
        </View>
      </View>

      <View style={styles.footer}>
        {form.submitError ? <Text style={[type.hint, styles.error]}>{form.submitError}</Text> : null}
        <Button label="Post story" onPress={form.submit} disabled={!form.canSubmit} loading={form.submitting} />
      </View>
    </Screen>
  );
}

function FieldHint({ error, hint }: { error?: string; hint: string }) {
  return <Text style={[type.hint, { color: error ? colors.danger : colors.textMuted }]}>{error ?? hint}</Text>;
}

const styles = StyleSheet.create({
  songRow: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  songText: { flex: 1 },
  songTitle: { ...type.body, fontWeight: '600' },
  songArtist: { ...type.hint, color: colors.textMuted },
  body: { minHeight: 140, textAlignVertical: 'top' },
  counter: { color: colors.textMuted, textAlign: 'right' },
  row: { flexDirection: 'row', gap: space.md },
  half: { flex: 1 },
  footer: { marginTop: 'auto', gap: space.sm },
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg },
  error: { color: colors.danger, textAlign: 'center' },
});
