import { ActivityIndicator, FlatList, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LoadingState } from '@/components/LoadingState';
import { QueueCard } from '@/features/moderation/QueueCard';
import { useModerationQueue } from '@/features/moderation/useModerationQueue';
import { makeStyles, space, useTheme } from '@/theme';

/** Moderators only (the API returns 403 to everyone else). */
export default function ModerationQueueScreen() {
  const styles = useStyles();
  const { colors } = useTheme();
  const { state, loadMore, decide, keyOf } = useModerationQueue();
  if (state.kind !== 'ready') return <LoadingState error={state.kind === 'error' ? state.message : null} />;

  return (
    <SafeAreaView style={styles.safe} edges={['bottom', 'left', 'right']}>
      <FlatList
        data={state.items}
        keyExtractor={keyOf}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => <QueueCard item={item} onDecide={(a) => decide(item, a)} />}
        ListEmptyComponent={<Text style={styles.empty}>Nothing to review. All quiet.</Text>}
        ListFooterComponent={state.loadingMore ? <ActivityIndicator color={colors.accent} /> : null}
        onEndReached={loadMore}
        onEndReachedThreshold={0.5}
      />
    </SafeAreaView>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  safe: { flex: 1, backgroundColor: colors.bg },
  list: { padding: space.lg, gap: space.md },
  empty: { ...type.subtitle, textAlign: 'center', marginTop: space.xl },
}));
