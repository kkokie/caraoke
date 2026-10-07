import { Text, View } from 'react-native';
import { Song, Story } from '@/api/client';
import { Screen } from '@/components/Screen';
import { TextField } from '@/components/TextField';
import { Button } from '@/components/Button';
import { Artwork } from '@/features/songs/Artwork';
import { makeStyles, space, useTheme } from '@/theme';
import { MAX_BODY, useComposeStory } from './useComposeStory';

/** The write/edit form. Same fields and rules for a new story and an edit. */
export function StoryForm({ song, existing }: { song: Song; existing?: Story }) {
  const styles = useStyles();
  const { type } = useTheme();
  const form = useComposeStory(song, existing);

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
        placeholder="Where were you the first time you heard it? Who does it remind you of? Write as much as you want."
        multiline
        autoFocus={!form.isEdit}
        style={styles.body}
        footer={<Text style={[type.hint, styles.counter]}>{form.body.length.toLocaleString()} / {MAX_BODY.toLocaleString()}</Text>}
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
        <Button
          label={form.isEdit ? 'Save changes' : 'Post story'}
          onPress={form.submit}
          disabled={!form.canSubmit}
          loading={form.submitting}
        />
      </View>
    </Screen>
  );
}

function FieldHint({ error, hint }: { error?: string; hint: string }) {
  const { colors, type } = useTheme();
  return <Text style={[type.hint, { color: error ? colors.danger : colors.textMuted }]}>{error ?? hint}</Text>;
}

const useStyles = makeStyles(({ colors, type }) => ({
  songRow: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  songText: { flex: 1 },
  songTitle: { ...type.body, fontWeight: '600' },
  songArtist: { ...type.hint, color: colors.textMuted },
  body: { minHeight: 220, textAlignVertical: 'top' },
  counter: { color: colors.textMuted, textAlign: 'right' },
  row: { flexDirection: 'row', gap: space.md },
  half: { flex: 1 },
  footer: { marginTop: 'auto', gap: space.sm },
  error: { color: colors.danger, textAlign: 'center' },
}));
