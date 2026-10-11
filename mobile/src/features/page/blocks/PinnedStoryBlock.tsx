import { Image, Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { StoryTile } from '@/api/client';
import { makeStyles, radius, space } from '@/theme';
import { BlockTitle } from './BlockTitle';

/** The one story they want you to read first. */
export function PinnedStoryBlock({ story }: { story: StoryTile }) {
  const styles = useStyles();
  const open = () => router.push({ pathname: '/story/[id]', params: { id: String(story.storyId) } });
  return (
    <View style={styles.wrap}>
      <BlockTitle>PINNED STORY</BlockTitle>
      <Pressable accessibilityRole="link" onPress={open} style={({ pressed }) => [styles.card, pressed && styles.pressed]}>
        {story.coverUrl
          ? <Image source={{ uri: story.coverUrl }} style={styles.cover} accessibilityIgnoresInvertColors />
          : <View style={styles.cover} />}
        <View style={styles.text}>
          <Text style={styles.song} numberOfLines={2}>{story.songTitle ?? 'A song'}</Text>
          <Text style={styles.artist} numberOfLines={1}>{story.artist ?? ''}</Text>
          <Text style={styles.read}>Read the story ›</Text>
        </View>
      </Pressable>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  card: {
    flexDirection: 'row', gap: space.md, padding: space.sm, alignItems: 'center',
    borderRadius: radius.md, backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.border,
  },
  pressed: { opacity: 0.7 },
  cover: { width: 72, height: 72, borderRadius: radius.md, backgroundColor: colors.border },
  text: { flex: 1, gap: 2 },
  song: { ...type.title, fontSize: 20, lineHeight: 24 },
  artist: { ...type.hint },
  read: { ...type.hint, color: colors.accent, marginTop: space.xs },
}));
