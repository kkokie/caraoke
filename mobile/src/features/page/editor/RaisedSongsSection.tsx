import { Pressable, Text, View } from 'react-native';
import { SongSummary } from '@/api/client';
import { Artwork } from '@/features/songs/Artwork';
import { SongPicker } from '@/features/songs/SongPicker';
import { makeStyles, space } from '@/theme';
import { MAX_RAISED } from './usePageEditor';

/** Up to five songs that raised you, in the order you add them. */
export function RaisedSongsSection({ songs, onAdd, onRemove }: {
  songs: SongSummary[];
  onAdd: (s: SongSummary) => void;
  onRemove: (id: number) => void;
}) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>SONGS THAT RAISED ME · {songs.length}/{MAX_RAISED}</Text>
      {songs.map((s, i) => (
        <View key={s.id} style={styles.row}>
          <Text style={styles.number}>{String(i + 1).padStart(2, '0')}</Text>
          <Artwork uri={s.artworkUrl} size={40} />
          <View style={styles.text}>
            <Text style={styles.title} numberOfLines={1}>{s.title}</Text>
            <Text style={styles.hint} numberOfLines={1}>{s.artist}</Text>
          </View>
          <Pressable accessibilityRole="button" accessibilityLabel={`Remove ${s.title}`} onPress={() => onRemove(s.id)} hitSlop={10}>
            <Text style={styles.remove}>×</Text>
          </Pressable>
        </View>
      ))}
      {songs.length < MAX_RAISED ? <SongPicker label="Add a song" onPick={onAdd} /> : null}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  label: { ...type.label },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  number: { ...type.mono, color: colors.accent, width: 20 },
  text: { flex: 1 },
  title: { ...type.body, fontWeight: '600' },
  hint: { ...type.hint },
  remove: { fontSize: 24, color: colors.textMuted, paddingHorizontal: space.xs },
}));
