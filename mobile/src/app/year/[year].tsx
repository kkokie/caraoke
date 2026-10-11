import { useCallback, useState } from 'react';
import { ActivityIndicator, FlatList, Text } from 'react-native';
import { Stack, useFocusEffect, useLocalSearchParams } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { api, FoundStory } from '@/api/client';
import { LoadingState } from '@/components/LoadingState';
import { FoundStoryCard } from '@/features/discover/FoundStoryCard';
import { makeStyles, space, useTheme } from '@/theme';

type State = { items: FoundStory[]; next: number | null; loading: boolean; error: string | null; loaded: boolean };

/** Every story from one year, newest first. Opened from "dig through the years". */
export default function YearScreen() {
  const styles = useStyles();
  const { colors } = useTheme();
  const year = Number(useLocalSearchParams<{ year: string }>().year);
  const [state, setState] = useState<State>({ items: [], next: null, loading: true, error: null, loaded: false });

  const load = useCallback(async (before: number | null) => {
    setState((s) => ({ ...s, loading: true }));
    try {
      const page = await api.storiesFromYear(year, before);
      setState((s) => ({
        items: before ? [...s.items, ...page.items] : page.items,
        next: page.nextCursor,
        loading: false,
        error: null,
        loaded: true,
      }));
    } catch (e) {
      setState((s) => ({ ...s, loading: false, loaded: true, error: e instanceof Error ? e.message : 'Couldn’t load' }));
    }
  }, [year]);

  useFocusEffect(useCallback(() => { load(null); }, [load]));

  if (!state.loaded) return <LoadingState error={null} />;
  return (
    <SafeAreaView style={styles.safe} edges={['bottom', 'left', 'right']}>
      <Stack.Screen options={{ title: String(year) }} />
      <FlatList
        data={state.items}
        keyExtractor={(f) => String(f.story.id)}
        contentContainerStyle={styles.list}
        ListHeaderComponent={<Text style={styles.title}>Stories from <Text style={styles.accent}>{year}</Text></Text>}
        renderItem={({ item }) => <FoundStoryCard found={item} />}
        onEndReached={() => state.next && !state.loading && load(state.next)}
        onEndReachedThreshold={0.5}
        ListFooterComponent={state.loading ? <ActivityIndicator color={colors.accent} /> : null}
        ListEmptyComponent={<Text style={styles.empty}>{state.error ?? `No stories from ${year} yet.`}</Text>}
      />
    </SafeAreaView>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  safe: { flex: 1, backgroundColor: colors.bg },
  list: { padding: space.lg, gap: space.lg },
  title: { ...type.title },
  accent: { fontFamily: type.lyric.fontFamily, color: colors.accent },
  empty: { ...type.subtitle, textAlign: 'center', marginTop: space.xl },
}));
