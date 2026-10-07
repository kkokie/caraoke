import { Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { FoundStory } from '@/api/client';
import { Artwork } from '@/features/songs/Artwork';
import { StoryCard } from '@/features/stories/StoryCard';
import { makeStyles, space } from '@/theme';

/** A story found away from its song page: the song it's about sits on top and opens the song. */
export function FoundStoryCard({ found }: { found: FoundStory }) {
  const styles = useStyles();
  const { song, story } = found;
  const openSong = () => router.push({ pathname: '/song/[id]', params: { id: String(story.songId) } });

  return (
    <View style={styles.wrap}>
      {song ? (
        <Pressable accessibilityRole="link" accessibilityLabel={`${song.title} by ${song.artist}`} onPress={openSong} style={styles.song}>
          <Artwork uri={song.artworkUrl} size={28} />
          <Text style={styles.songText} numberOfLines={1}>
            {song.title.toUpperCase()} · {song.artist.toUpperCase()}
          </Text>
        </Pressable>
      ) : null}
      <StoryCard story={story} />
    </View>
  );
}

const useStyles = makeStyles(({ type }) => ({
  wrap: { gap: space.sm },
  song: { flexDirection: 'row', alignItems: 'center', gap: space.sm, minHeight: 36 },
  songText: { ...type.label, flex: 1 },
}));
