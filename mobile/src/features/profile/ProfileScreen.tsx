import { ReactNode } from 'react';
import { ActivityIndicator, FlatList, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '@/components/Button';
import { colors, space, type } from '@/theme';
import { ProfileHeader } from './ProfileHeader';
import { StoryTile } from './StoryTile';
import { useProfile } from './useProfile';

/**
 * The whole profile page (header + 3-column grid). Used by both the Profile tab (you)
 * and /user/[handle] (anyone else).
 */
export function ProfileScreen({ handle, edges = ['left', 'right'] }: { handle: string; edges?: ('top' | 'left' | 'right' | 'bottom')[] }) {
  const p = useProfile(handle);

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
  return (
    <SafeAreaView style={styles.safe} edges={edges}>
      <FlatList
        data={p.tiles}
        keyExtractor={(t) => String(t.storyId)}
        numColumns={3}
        renderItem={({ item }) => <StoryTile tile={item} />}
        ListHeaderComponent={<View style={styles.header}><ProfileHeader profile={profile} /></View>}
        ListEmptyComponent={<EmptyGrid mine={profile.me} />}
        ListFooterComponent={p.loadingMore ? <ActivityIndicator color={colors.accent} style={styles.footer} /> : null}
        onEndReached={p.loadMore}
        onEndReachedThreshold={0.5}
      />
    </SafeAreaView>
  );
}

function EmptyGrid({ mine }: { mine: boolean }) {
  return (
    <View style={styles.empty}>
      <Text style={[type.subtitle, styles.center]}>
        {mine ? 'Your stories will show up here.' : 'No stories yet.'}
      </Text>
      {mine ? <Button label="Find a song" onPress={() => router.push('/search')} /> : null}
    </View>
  );
}

function Centered({ children }: { children: ReactNode }) {
  return <View style={styles.centered}>{children}</View>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.bg },
  header: { paddingHorizontal: space.lg, paddingTop: space.md },
  empty: { padding: space.lg, gap: space.md },
  center: { textAlign: 'center' },
  footer: { marginVertical: space.lg },
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg, gap: space.md },
  error: { color: colors.danger, textAlign: 'center' },
});
