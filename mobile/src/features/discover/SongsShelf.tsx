import { Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { SongWithCount } from '@/api/client';
import { Artwork } from '@/features/songs/Artwork';
import { makeStyles, space } from '@/theme';

/** "Songs full of stories": the rooms with the most memories in them. */
export function SongsShelf({ songs }: { songs: SongWithCount[] }) {
  const styles = useStyles();
  return (
    <View>
      {songs.map(({ song, stories }) => (
        <Pressable
          key={song.id}
          accessibilityRole="link"
          onPress={() => router.push({ pathname: '/song/[id]', params: { id: String(song.id) } })}
          style={({ pressed }) => [styles.row, pressed && styles.pressed]}>
          <Artwork uri={song.artworkUrl} size={44} />
          <View style={styles.text}>
            <Text style={styles.title} numberOfLines={1}>{song.title}</Text>
            <Text style={styles.artist} numberOfLines={1}>{song.artist}</Text>
          </View>
          <Text style={styles.count}>{stories} {stories === 1 ? 'story' : 'stories'}</Text>
        </Pressable>
      ))}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md, paddingVertical: space.sm },
  pressed: { opacity: 0.6 },
  text: { flex: 1 },
  title: { ...type.body, fontWeight: '600' },
  artist: { ...type.hint },
  count: { ...type.mono, color: colors.accent },
}));
