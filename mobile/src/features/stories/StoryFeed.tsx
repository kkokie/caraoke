import { ActivityIndicator, Text, View } from 'react-native';
import { Button } from '@/components/Button';
import { makeStyles, space, useTheme } from '@/theme';
import { StoryCard } from './StoryCard';
import { useStoryFeed } from './useStoryFeed';

/**
 * The stories section of a song page. Renders inside the page's ScrollView
 * (a plain map, not a nested FlatList), which is fine at v1 page sizes.
 */
export function StoryFeed({ songId }: { songId: number }) {
  const styles = useStyles();
  const { colors, type } = useTheme();
  const feed = useStoryFeed(songId);

  if (feed.status === 'loading') return <ActivityIndicator color={colors.accent} style={styles.spacer} />;
  if (feed.status === 'error') {
    return (
      <View style={styles.message}>
        <Text style={[type.subtitle, styles.error]}>{feed.error}</Text>
        <Button label="Try again" variant="ghost" onPress={feed.refresh} />
      </View>
    );
  }
  if (feed.items.length === 0) {
    return <Text style={[type.subtitle, styles.spacer, styles.center]}>No stories yet. Be the first to share what this song means to you.</Text>;
  }

  return (
    <View style={styles.list}>
      {feed.items.map((story) => (
        <StoryCard key={story.id} story={story} onDeleted={feed.removeLocally} />
      ))}
      {feed.nextCursor ? (
        <Button label="Load more stories" variant="ghost" onPress={feed.loadMore} loading={feed.loadingMore} />
      ) : null}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  list: { gap: space.md },
  spacer: { marginVertical: space.lg },
  center: { textAlign: 'center' },
  message: { gap: space.md, alignItems: 'center', marginVertical: space.lg },
  error: { color: colors.danger, textAlign: 'center' },
}));
