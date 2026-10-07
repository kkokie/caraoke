import { ReactNode, useCallback, useState } from 'react';
import { ActivityIndicator, FlatList, Text, View } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '@/components/Button';
import { makeStyles, space, useTheme } from '@/theme';
import { GridTab, GridTabs } from './GridTabs';
import { ProfileHeader } from './ProfileHeader';
import { StoryTile } from './StoryTile';
import { useFeltTiles } from './useFeltTiles';
import { useProfile } from './useProfile';

/**
 * The whole profile page (header + 3-column grid). Used by both the Profile tab (you)
 * and /user/[handle] (anyone else). On your own profile, a Stories | Felt switcher
 * shows the stories you resonated with (private to you).
 */
export function ProfileScreen({ handle, edges = ['left', 'right'] }: { handle: string; edges?: ('top' | 'left' | 'right' | 'bottom')[] }) {
  const styles = useStyles();
  const { colors, type } = useTheme();
  const p = useProfile(handle);
  const felt = useFeltTiles();
  const [tab, setTab] = useState<GridTab>('stories');
  const showingFelt = tab === 'felt' && !!p.profile?.me;

  // Keep the Felt grid fresh when you come back after feeling something new
  useFocusEffect(
    useCallback(() => {
      if (showingFelt) felt.refresh();
    }, [showingFelt, felt.refresh]),
  );

  if (p.status === 'loading') return <Centered><ActivityIndicator color={colors.accent} /></Centered>;
  if (p.status === 'error' || !p.profile) {
    return (
      <Centered>
        <Text style={[type.subtitle, styles.error]}>{p.error ?? 'Couldn’t load this profile'}</Text>
        <Button label="Try again" variant="ghost" onPress={p.refresh} />
      </Centered>
    );
  }

  const profile = p.profile;
  const selectTab = (t: GridTab) => {
    setTab(t);
    if (t === 'felt' && felt.status === 'idle') felt.refresh();
  };
  const grid = showingFelt ? felt : p;

  return (
    <SafeAreaView style={styles.safe} edges={edges}>
      <FlatList
        data={grid.tiles}
        keyExtractor={(t) => String(t.storyId)}
        numColumns={3}
        renderItem={({ item }) => <StoryTile tile={item} />}
        ListHeaderComponent={
          <View style={styles.header}>
            <ProfileHeader profile={profile} />
            {profile.me ? <GridTabs tab={tab} onChange={selectTab} /> : null}
          </View>
        }
        ListEmptyComponent={showingFelt
          ? <FeltEmpty loading={felt.status === 'loading' || felt.status === 'idle'} />
          : <EmptyGrid mine={profile.me} />}
        ListFooterComponent={grid.loadingMore ? <ActivityIndicator color={colors.accent} style={styles.footer} /> : null}
        onEndReached={grid.loadMore}
        onEndReachedThreshold={0.5}
      />
    </SafeAreaView>
  );
}

function EmptyGrid({ mine }: { mine: boolean }) {
  const styles = useStyles();
  const { type } = useTheme();
  return (
    <View style={styles.empty}>
      <Text style={[type.subtitle, styles.center]}>
        {mine ? 'Your stories will show up here.' : 'No stories yet.'}
      </Text>
      {mine ? <Button label="Find a song" onPress={() => router.push('/search')} /> : null}
    </View>
  );
}

function FeltEmpty({ loading }: { loading: boolean }) {
  const styles = useStyles();
  const { colors, type } = useTheme();
  if (loading) return <ActivityIndicator color={colors.accent} style={styles.footer} />;
  return (
    <View style={styles.empty}>
      <Text style={[type.subtitle, styles.center]}>
        Stories you felt will show up here. Only you can see this.
      </Text>
    </View>
  );
}

function Centered({ children }: { children: ReactNode }) {
  const styles = useStyles();
  return <View style={styles.centered}>{children}</View>;
}

const useStyles = makeStyles(({ colors, type }) => ({
  safe: { flex: 1, backgroundColor: colors.bg },
  header: { paddingHorizontal: space.lg, paddingTop: space.md },
  empty: { padding: space.lg, gap: space.md },
  center: { textAlign: 'center' },
  footer: { marginVertical: space.lg },
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg, gap: space.md },
  error: { color: colors.danger, textAlign: 'center' },
}));
