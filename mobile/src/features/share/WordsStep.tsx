import { Text, TextInput, View } from 'react-native';
import { makeStyles, space, useTheme } from '@/theme';
import { MAX_BODY, MAX_LYRIC, ShareDraft } from './useShareDraft';

/** Step 2: the line that gets you (optional, in the lyric font) and the story itself. */
export function WordsStep({ draft }: { draft: ShareDraft }) {
  const styles = useStyles();
  const { colors, scheme } = useTheme();
  const input = { placeholderTextColor: colors.textMuted, selectionColor: colors.accent, keyboardAppearance: scheme };

  return (
    <View style={styles.wrap}>
      <View style={styles.field}>
        <Text style={styles.label}>THE LINE THAT GETS YOU · OPTIONAL</Text>
        <TextInput
          {...input}
          value={draft.lyric}
          onChangeText={draft.setLyric}
          placeholder="Type the lyric from memory"
          multiline
          maxLength={MAX_LYRIC}
          style={styles.lyric}
          accessibilityLabel="The lyric line that gets you"
        />
        <Footer left="A line or two, from memory." right={`${draft.lyric.length} / ${MAX_LYRIC}`} />
      </View>

      <View style={styles.field}>
        <Text style={styles.label}>YOUR STORY</Text>
        <TextInput
          {...input}
          value={draft.body}
          onChangeText={draft.setBody}
          placeholder="Where were you the first time you heard it? Who does it bring back?"
          multiline
          autoFocus={!draft.isEdit && !draft.lyric}
          style={styles.body}
          accessibilityLabel="Your story"
        />
        <Footer
          left="As long as you need. Up to a whole essay."
          right={`${draft.body.length.toLocaleString()} / ${MAX_BODY.toLocaleString()}`}
        />
      </View>
    </View>
  );
}

function Footer({ left, right }: { left: string; right: string }) {
  const styles = useStyles();
  return (
    <View style={styles.footer}>
      <Text style={styles.hint}>{left}</Text>
      <Text style={styles.count}>{right}</Text>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.xl },
  field: { gap: space.sm },
  label: { ...type.label },
  lyric: {
    ...type.lyric,
    fontSize: 26,
    lineHeight: 32,
    color: colors.accent,
    paddingVertical: space.xs,
    borderBottomWidth: 1,
    borderBottomColor: colors.accent,
  },
  body: { ...type.story, fontSize: 22, lineHeight: 30, minHeight: 240, textAlignVertical: 'top' },
  footer: { flexDirection: 'row', justifyContent: 'space-between', gap: space.md },
  hint: { ...type.hint, flex: 1 },
  count: { ...type.mono },
}));
