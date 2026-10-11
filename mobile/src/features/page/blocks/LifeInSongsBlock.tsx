import { Image, Pressable, ScrollView, Text, View } from 'react-native';
import { router } from 'expo-router';
import { StoryTile } from '@/api/client';
import { makeStyles, radius, space } from '@/theme';
import { BlockTitle } from './BlockTitle';

/** Their stories laid along the years the memories are from, oldest first. Scrolls sideways. */
export function LifeInSongsBlock({ stops }: { stops: { year: number; story: StoryTile }[] }) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <BlockTitle>MY LIFE IN SONGS</BlockTitle>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.row}>
        {stops.map(({ year, story }, i) => (
          <Pressable
            key={story.storyId}
            accessibilityRole="link"
            accessibilityLabel={`${year}: ${story.songTitle ?? 'a song'}`}
            onPress={() => router.push({ pathname: '/story/[id]', params: { id: String(story.storyId) } })}
            style={({ pressed }) => [styles.stop, pressed && styles.pressed]}>
            <Text style={[styles.year, i > 0 && stops[i - 1].year === year && styles.sameYear]}>{year}</Text>
            {story.coverUrl
              ? <Image source={{ uri: story.coverUrl }} style={styles.cover} accessibilityIgnoresInvertColors />
              : <View style={styles.cover} />}
            <Text style={styles.song} numberOfLines={2}>{story.songTitle ?? ''}</Text>
          </Pressable>
        ))}
      </ScrollView>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  row: { gap: space.md, paddingRight: space.lg },
  stop: { width: 88, gap: space.xs },
  pressed: { opacity: 0.7 },
  year: { ...type.mono, color: colors.accent },
  sameYear: { opacity: 0 },                                // the year shows once, on its first stop
  cover: { width: 88, height: 88, borderRadius: radius.md, backgroundColor: colors.border },
  song: { ...type.hint, color: colors.text },
}));
