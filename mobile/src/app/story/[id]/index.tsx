import { Pressable, StyleSheet, Text, View } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Song, Story } from '@/api/client';
import { Screen } from '@/components/Screen';
import { LoadingState } from '@/components/LoadingState';
import { Artwork } from '@/features/songs/Artwork';
import { StoryCard } from '@/features/stories/StoryCard';
import { useStoryWithSong } from '@/features/stories/useStoryWithSong';
import { colors, radius, space, type } from '@/theme';

/** One story on its own page (opened from a profile tile). */
export default function StoryScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = useStoryWithSong(Number(id));

  if (state.kind !== 'ready') return <LoadingState error={state.kind === 'error' ? state.message : null} />;
  return <StoryPage story={state.story} song={state.song} />;
}

function StoryPage({ story, song }: { story: Story; song: Song }) {
  const openSong = () => router.push({ pathname: '/song/[id]', params: { id: String(song.id) } });

  return (
    <Screen>
      <Pressable accessibilityRole="link" onPress={openSong} style={({ pressed }) => [styles.song, pressed && styles.pressed]}>
        <Artwork uri={song.artworkUrl} size={64} radius={radius.md} />
        <View style={styles.songText}>
          <Text style={styles.title} numberOfLines={1}>{song.title}</Text>
          <Text style={styles.artist} numberOfLines={1}>{song.artist}</Text>
          <Text style={styles.link}>See all stories ›</Text>
        </View>
      </Pressable>

      <StoryCard story={story} onDeleted={() => router.back()} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  song: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  pressed: { opacity: 0.7 },
  songText: { flex: 1, gap: 2 },
  title: { ...type.body, fontWeight: '700' },
  artist: { ...type.hint, color: colors.textMuted },
  link: { ...type.hint, color: colors.accent, marginTop: space.xs },
});
