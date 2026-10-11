import { ReactNode, useState } from 'react';
import { ActivityIndicator, Pressable, Text, View } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Song } from '@/api/client';
import { Screen } from '@/components/Screen';
import { Button } from '@/components/Button';
import { Artwork } from '@/features/songs/Artwork';
import { ListenButtons } from '@/features/songs/ListenButtons';
import { MomentsTimeline } from '@/features/songs/MomentsTimeline';
import { PreviewPlayer } from '@/features/songs/PreviewPlayer';
import { useSong } from '@/features/songs/useSong';
import { StoryFeed } from '@/features/stories/StoryFeed';
import { formatDuration } from '@/lib/format';
import { makeStyles, radius, space, useTheme } from '@/theme';

export default function SongScreen() {
  const styles = useStyles();
  const { colors, type } = useTheme();
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = useSong(Number(id));

  if (state.kind === 'loading') return <Centered><ActivityIndicator color={colors.accent} /></Centered>;
  if (state.kind === 'error') return <Centered><Text style={[type.subtitle, styles.error]}>{state.message}</Text></Centered>;
  return <SongPage song={state.song} />;
}

function SongPage({ song }: { song: Song }) {
  const styles = useStyles();
  const { type } = useTheme();
  const compose = () => router.push({ pathname: '/song/[id]/compose', params: { id: String(song.id) } });
  const [moment, setMoment] = useState<number | null>(null);   // a timeline dot narrows the feed

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

      <PreviewPlayer songId={song.id} />
      <ListenButtons links={song.listen} />
      <MomentsTimeline songId={song.id} durationSec={song.durationSec} selected={moment} onSelect={setMoment} />

      <View style={styles.stories}>
        <View style={styles.storiesHead}>
          <Text style={type.label}>{moment == null ? 'STORIES' : `STORIES AT ${formatDuration(moment)}`}</Text>
          {moment != null ? (
            <Pressable accessibilityRole="button" onPress={() => setMoment(null)} hitSlop={10}>
              <Text style={styles.showAll}>Show all</Text>
            </Pressable>
          ) : null}
        </View>
        <Button label="Share your story" onPress={compose} />
        <StoryFeed songId={song.id} moment={moment} />
      </View>
    </Screen>
  );
}

function Centered({ children }: { children: ReactNode }) {
  const styles = useStyles();
  return <View style={styles.centered}>{children}</View>;
}

const useStyles = makeStyles(({ colors, type }) => ({
  hero: { alignItems: 'center', gap: space.md, marginTop: space.md },
  titles: { alignItems: 'center', gap: space.xs },
  title: { ...type.title, fontSize: 28, lineHeight: 32, textAlign: 'center' },
  center: { textAlign: 'center' },
  stories: { gap: space.md, marginTop: space.sm },
  storiesHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  showAll: { ...type.label, color: colors.accent },
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg },
  error: { color: colors.danger, textAlign: 'center' },
}));
