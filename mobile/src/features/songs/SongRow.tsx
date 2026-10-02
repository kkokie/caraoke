import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { SongSearchResult } from '@/api/client';
import { formatDuration } from '@/lib/format';
import { colors, space, type } from '@/theme';
import { Artwork } from './Artwork';

type Props = { song: SongSearchResult; loading: boolean; onPress: () => void };

export function SongRow({ song, loading, onPress }: Props) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`${song.title} by ${song.artist}`}
      onPress={onPress}
      style={({ pressed }) => [styles.row, pressed && styles.pressed]}>
      <Artwork uri={song.artworkUrl} size={52} />
      <View style={styles.text}>
        <Text style={styles.title} numberOfLines={1}>{song.title}</Text>
        <Text style={styles.sub} numberOfLines={1}>
          {song.artist}{song.album ? ` · ${song.album}` : ''}
        </Text>
      </View>
      {loading
        ? <ActivityIndicator color={colors.accent} />
        : <Text style={styles.sub}>{formatDuration(song.durationSec)}</Text>}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md, paddingVertical: space.sm },
  pressed: { opacity: 0.6 },
  text: { flex: 1, gap: 2 },
  title: { ...type.body, fontWeight: '600' },
  sub: { ...type.hint, color: colors.textMuted },
});
