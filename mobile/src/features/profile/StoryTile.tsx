import { Image, Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { StoryTile as Tile } from '@/api/client';
import { makeStyles } from '@/theme';

/** One square. The image is the album cover today; the story's first photo once photos land. */
export function StoryTile({ tile }: { tile: Tile }) {
  const styles = useStyles();
  const open = () => router.push({ pathname: '/story/[id]', params: { id: String(tile.storyId) } });
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`Story about ${tile.songTitle ?? 'a song'}`}
      onPress={open}
      style={({ pressed }) => [styles.tile, pressed && styles.pressed]}>
      {tile.coverUrl
        ? <Image source={{ uri: tile.coverUrl }} style={styles.image} accessibilityIgnoresInvertColors />
        : <View style={[styles.image, styles.placeholder]}><Text style={styles.note}>♪</Text></View>}
      {tile.resonanceCount > 0 ? (
        <View style={styles.badge}>
          <Text style={styles.badgeText}>♥ {tile.resonanceCount}</Text>
        </View>
      ) : null}
    </Pressable>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  tile: { flex: 1 / 3, aspectRatio: 1, padding: 1 },
  pressed: { opacity: 0.7 },
  image: { flex: 1, backgroundColor: colors.surface },
  placeholder: { alignItems: 'center', justifyContent: 'center' },
  note: { color: colors.textMuted, fontSize: 28 },
  badge: {
    position: 'absolute',
    right: 6,
    bottom: 6,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 999,
    backgroundColor: 'rgba(20,17,15,0.75)',
  },
  badgeText: { color: colors.accent, fontSize: 11, fontWeight: '700' },
}));
