import { ReactNode } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { Song } from '@/api/client';
import { Screen } from '@/components/Screen';
import { Artwork } from '@/features/songs/Artwork';
import { ListenButtons } from '@/features/songs/ListenButtons';
import { useSong } from '@/features/songs/useSong';
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
  return (
    <Screen>
      <View style={styles.hero}>
        <Artwork uri={song.artworkUrl} size={240} radius={radius.lg} />
        <View style={styles.titles}>
          <Text style={styles.title}>{song.title}</Text>
          <Text style={type.subtitle}>
            {song.artist}{song.album ? ` · ${song.album}` : ''}{song.durationSec ? ` · ${formatDuration(song.durationSec)}` : ''}
          </Text>
        </View>
      </View>

      <ListenButtons links={song.listen} />

      <View style={styles.storiesStub}>
        <Text style={type.label}>STORIES</Text>
        <Text style={type.subtitle}>No stories yet. Soon you’ll be able to share what this song means to you.</Text>
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
  storiesStub: {
    gap: space.sm,
    padding: space.md,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    borderStyle: 'dashed',
  },
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg },
  error: { color: colors.danger, textAlign: 'center' },
});
