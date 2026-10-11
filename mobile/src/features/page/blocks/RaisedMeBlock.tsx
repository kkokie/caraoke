import { Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { SongSummary } from '@/api/client';
import { Artwork } from '@/features/songs/Artwork';
import { makeStyles, space } from '@/theme';
import { BlockTitle } from './BlockTitle';

/** Up to five songs that raised them, in their order. */
export function RaisedMeBlock({ songs }: { songs: SongSummary[] }) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <BlockTitle>SONGS THAT RAISED ME</BlockTitle>
      {songs.map((song, i) => (
        <Pressable
          key={song.id}
          accessibilityRole="link"
          onPress={() => router.push({ pathname: '/song/[id]', params: { id: String(song.id) } })}
          style={({ pressed }) => [styles.row, pressed && styles.pressed]}>
          <Text style={styles.number}>{String(i + 1).padStart(2, '0')}</Text>
          <Artwork uri={song.artworkUrl} size={40} />
          <View style={styles.text}>
            <Text style={styles.title} numberOfLines={1}>{song.title}</Text>
            <Text style={styles.artist} numberOfLines={1}>{song.artist}</Text>
          </View>
        </Pressable>
      ))}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  pressed: { opacity: 0.7 },
  number: { ...type.mono, color: colors.accent, width: 20 },
  text: { flex: 1 },
  title: { ...type.body, fontWeight: '600' },
  artist: { ...type.hint },
}));
