import { ReactNode } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Song } from '@/api/client';
import { Screen } from '@/components/Screen';
import { Button } from '@/components/Button';
import { Artwork } from '@/features/songs/Artwork';
import { ListenButtons } from '@/features/songs/ListenButtons';
import { useSong } from '@/features/songs/useSong';
import { StoryFeed } from '@/features/stories/StoryFeed';
import { formatDuration } from '@/lib/format';
import { colors, radius, space, type } from '@/theme';

export default function SongScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = useSong(Number(id));

  if (state.kind === 'loading') return <Centered><ActivityIndicator color={colors.accent} /></Centered>;
  if (state.kind === 'error') return <Centered><Text style={[type.subtitle, styles.error]}>{state.message}</Text></Centered>;
  return <SongPage song={state.song} />;
}

function SongPage({ song }: { song: Song }) {
  const compose = () => router.push({ pathname: '/song/[id]/compose', params: { id: String(song.id) } });

  return (
    <Screen>
      <View style={styles.hero}>
        <Artwork uri={song.artworkUrl} size={200} radius={radius.lg} />
        <View style={styles.titles}>
          <Text style={styles.title}>{song.title}</Text>
          <Text style={[type.subtitle, styles.center]}>
            {song.artist}{song.album ? ` · ${song.album}` : ''}{song.durationSec ? ` · ${formatDuration(song.durationSec)}` : ''}
          </Text>
        </View>
      </View>

      <ListenButtons links={song.listen} />

      <View style={styles.stories}>
        <Text style={type.label}>STORIES</Text>
        <Button label="Share your story" onPress={compose} />
        <StoryFeed songId={song.id} />
      </View>
    </Screen>
  );
}

function Centered({ children }: { children: ReactNode }) {
  return <View style={styles.centered}>{children}</View>;
}

const styles = StyleSheet.create({
  hero: { alignItems: 'center', gap: space.md, marginTop: space.md },
  titles: { alignItems: 'center', gap: space.xs },
  title: { ...type.title, fontSize: 26, textAlign: 'center' },
  center: { textAlign: 'center' },
  stories: { gap: space.md, marginTop: space.sm },
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg },
  error: { color: colors.danger, textAlign: 'center' },
});
