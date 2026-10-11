import { FlatList, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Avatar } from '@/components/Avatar';
import { Button } from '@/components/Button';
import { LoadingState } from '@/components/LoadingState';
import { useBlockedAccounts } from '@/features/safety/useBlockedAccounts';
import { makeStyles, space } from '@/theme';

/** People you blocked. Unblocking doesn't bring back follows. */
export default function BlockedAccountsScreen() {
  const styles = useStyles();
  const { state, unblockOne } = useBlockedAccounts();
  if (state.kind !== 'ready') return <LoadingState error={state.kind === 'error' ? state.message : null} />;

  return (
    <SafeAreaView style={styles.safe} edges={['bottom', 'left', 'right']}>
      <FlatList
        data={state.accounts}
        keyExtractor={(a) => a.handle}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <Avatar name={item.displayName} url={item.avatarUrl} size={40} />
            <View style={styles.text}>
              <Text style={styles.name} numberOfLines={1}>{item.displayName}</Text>
              <Text style={styles.handle} numberOfLines={1}>@{item.handle}</Text>
            </View>
            <View style={styles.button}>
              <Button label="Unblock" variant="ghost" onPress={() => unblockOne(item.handle)} />
            </View>
          </View>
        )}
        ListEmptyComponent={<Text style={styles.empty}>You haven’t blocked anyone.</Text>}
      />
    </SafeAreaView>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  safe: { flex: 1, backgroundColor: colors.bg },
  list: { padding: space.lg, gap: space.md },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  text: { flex: 1 },
  name: { ...type.body, fontWeight: '600' },
  handle: { ...type.hint },
  button: { width: 120 },
  empty: { ...type.subtitle, textAlign: 'center', marginTop: space.xl },
}));
