import { ReactNode } from 'react';
import { ActivityIndicator, FlatList, StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Author } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { useResonators } from '@/features/resonance/useResonators';
import { colors, space, type } from '@/theme';

/** The people who felt a story too: the "connect through the same stories" moment. */
export default function FeltThisScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = useResonators(Number(id));

  if (state.kind === 'loading') return <Centered><ActivityIndicator color={colors.accent} /></Centered>;
  if (state.kind === 'error') return <Centered><Text style={[type.subtitle, styles.error]}>{state.message}</Text></Centered>;

  return (
    <SafeAreaView style={styles.safe} edges={['bottom', 'left', 'right']}>
      <FlatList
        data={state.people}
        keyExtractor={(p) => p.handle}
        contentContainerStyle={styles.list}
        ListHeaderComponent={<Text style={[type.subtitle, styles.header]}>They’ve been there too.</Text>}
        renderItem={({ item }) => <PersonRow person={item} />}
        ListEmptyComponent={<Text style={[type.subtitle, styles.center]}>No one yet.</Text>}
      />
    </SafeAreaView>
  );
}

function PersonRow({ person }: { person: Author }) {
  return (
    <View style={styles.row}>
      <Avatar name={person.displayName} size={40} />
      <View style={styles.text}>
        <Text style={styles.name} numberOfLines={1}>{person.displayName}</Text>
        <Text style={styles.handle} numberOfLines={1}>@{person.handle}</Text>
      </View>
    </View>
  );
}

function Centered({ children }: { children: ReactNode }) {
  return <View style={styles.centered}>{children}</View>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.bg },
  list: { padding: space.lg, gap: space.md },
  header: { marginBottom: space.sm },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  text: { flex: 1 },
  name: { ...type.body, fontWeight: '600' },
  handle: { ...type.hint, color: colors.textMuted },
  center: { textAlign: 'center', marginTop: space.xl },
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg },
  error: { color: colors.danger, textAlign: 'center' },
});
