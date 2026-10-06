import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Author } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { LoadingState } from '@/components/LoadingState';
import { colors, space, type } from '@/theme';
import { PeopleState } from './usePeople';

type Props = { state: PeopleState; header?: string; empty: string };

/** A tappable list of people. Each row opens that person's profile. */
export function PeopleList({ state, header, empty }: Props) {
  if (state.kind !== 'ready') return <LoadingState error={state.kind === 'error' ? state.message : null} />;

  return (
    <SafeAreaView style={styles.safe} edges={['bottom', 'left', 'right']}>
      <FlatList
        data={state.people}
        keyExtractor={(p) => p.handle}
        contentContainerStyle={styles.list}
        ListHeaderComponent={header ? <Text style={[type.subtitle, styles.header]}>{header}</Text> : null}
        renderItem={({ item }) => <PersonRow person={item} />}
        ListEmptyComponent={<Text style={[type.subtitle, styles.center]}>{empty}</Text>}
      />
    </SafeAreaView>
  );
}

function PersonRow({ person }: { person: Author }) {
  const open = () => router.push({ pathname: '/user/[handle]', params: { handle: person.handle } });
  return (
    <Pressable accessibilityRole="link" onPress={open} style={({ pressed }) => [styles.row, pressed && styles.pressed]}>
      <Avatar name={person.displayName} size={40} />
      <View style={styles.text}>
        <Text style={styles.name} numberOfLines={1}>{person.displayName}</Text>
        <Text style={styles.handle} numberOfLines={1}>@{person.handle}</Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.bg },
  list: { padding: space.lg, gap: space.md },
  header: { marginBottom: space.sm },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  pressed: { opacity: 0.6 },
  text: { flex: 1 },
  name: { ...type.body, fontWeight: '600' },
  handle: { ...type.hint, color: colors.textMuted },
  center: { textAlign: 'center', marginTop: space.xl },
});
