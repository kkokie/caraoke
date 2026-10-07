import { ActivityIndicator, Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/Button';
import { Screen } from '@/components/Screen';
import { FoundStoryCard } from '@/features/discover/FoundStoryCard';
import { SongsShelf } from '@/features/discover/SongsShelf';
import { useDig } from '@/features/discover/useDig';
import { useShelves } from '@/features/discover/useShelves';
import { YearStrip } from '@/features/discover/YearStrip';
import { partOfWeek } from '@/lib/when';
import { makeStyles, radius, space, useTheme } from '@/theme';

/** Discover: treasure hunting for stories. Dig for one, dig through the years, or open a song. */
export default function DiscoverScreen() {
  const styles = useStyles();
  const shelves = useShelves();

  return (
    <Screen>
      <View style={styles.header}>
        <Text style={styles.when}>{partOfWeek()}</Text>
        <Text style={styles.title}>Go <Text style={styles.accent}>digging.</Text></Text>
      </View>

      <SearchEntry />
      <FoundSection />

      {shelves.years.length > 0 ? (
        <View style={styles.section}>
          <Text style={styles.label}>DIG THROUGH THE YEARS</Text>
          <YearStrip years={shelves.years} />
        </View>
      ) : null}

      {shelves.songs.length > 0 ? (
        <View style={styles.section}>
          <Text style={styles.label}>SONGS FULL OF STORIES</Text>
          <SongsShelf songs={shelves.songs} />
        </View>
      ) : null}
    </Screen>
  );
}

/** Looks like a search box; opens the full search (songs and people). */
function SearchEntry() {
  const styles = useStyles();
  return (
    <Pressable accessibilityRole="search" onPress={() => router.push('/search')} style={styles.search}>
      <Text style={styles.searchText}>A song or a person</Text>
    </Pressable>
  );
}

function FoundSection() {
  const styles = useStyles();
  const { colors } = useTheme();
  const { state, dig, startOver, hasSeenAny } = useDig();

  return (
    <View style={styles.section}>
      <View style={styles.sectionHead}>
        <Text style={styles.label}>A STORY YOU FOUND</Text>
        {state.kind === 'found' ? (
          <Pressable accessibilityRole="button" onPress={dig} hitSlop={8} style={styles.digAgain}>
            <Text style={styles.digAgainText}>↻ Dig again</Text>
          </Pressable>
        ) : null}
      </View>
      {state.kind === 'digging' ? <ActivityIndicator color={colors.accent} style={styles.spinner} /> : null}
      {state.kind === 'found' ? <FoundStoryCard found={state.found} /> : null}
      {state.kind === 'empty' ? (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>
            {hasSeenAny()
              ? 'You’ve dug up every story there is, for now.'
              : 'No stories to find yet. Be the first: tap + and share the song that raised you.'}
          </Text>
          {hasSeenAny() ? <Button label="Start over" variant="ghost" onPress={startOver} /> : null}
        </View>
      ) : null}
      {state.kind === 'error' ? (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>{state.message}</Text>
          <Button label="Try again" variant="ghost" onPress={dig} />
        </View>
      ) : null}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  header: { gap: space.xs, marginTop: space.md },
  when: { ...type.label },
  title: { ...type.title },
  accent: { fontFamily: type.lyric.fontFamily, color: colors.accent },
  search: {
    minHeight: 48,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    paddingHorizontal: space.md,
    justifyContent: 'center',
  },
  searchText: { ...type.body, color: colors.textMuted },
  section: { gap: space.sm },
  sectionHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  label: { ...type.label },
  digAgain: { minHeight: 36, justifyContent: 'center', paddingHorizontal: space.sm },
  digAgainText: { ...type.body, fontSize: 15, fontWeight: '600', color: colors.accent },
  spinner: { paddingVertical: space.xl },
  empty: { gap: space.md, paddingVertical: space.md },
  emptyText: { ...type.story, color: colors.textMuted },
}));
