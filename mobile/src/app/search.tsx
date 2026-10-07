import { useState } from 'react';
import { ActivityIndicator, FlatList, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { TextField } from '@/components/TextField';
import { SongRow } from '@/features/songs/SongRow';
import { useOpenSong } from '@/features/songs/useOpenSong';
import { SongSearchState, useSongSearch } from '@/features/songs/useSongSearch';
import { makeStyles, space, useTheme } from '@/theme';

export default function SearchScreen() {
  const styles = useStyles();
  const [query, setQuery] = useState('');
  const search = useSongSearch(query);
  const { open, openingId } = useOpenSong();
  const items = search.kind === 'results' ? search.items : [];

  return (
    <SafeAreaView style={styles.safe} edges={['bottom', 'left', 'right']}>
      <View style={styles.searchBox}>
        <TextField
          label="Find a song"
          value={query}
          onChangeText={setQuery}
          placeholder="Title or artist"
          autoFocus
          autoCorrect={false}
          returnKeyType="search"
          clearButtonMode="while-editing"
        />
      </View>
      <FlatList
        data={items}
        keyExtractor={(s) => s.appleId}
        keyboardShouldPersistTaps="handled"
        keyboardDismissMode="on-drag"
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <SongRow song={item} loading={openingId === item.appleId} onPress={() => open(item)} />
        )}
        ListEmptyComponent={<SearchEmptyState state={search} />}
      />
    </SafeAreaView>
  );
}

function SearchEmptyState({ state }: { state: SongSearchState }) {
  const styles = useStyles();
  const { colors, type } = useTheme();
  if (state.kind === 'searching') return <ActivityIndicator color={colors.accent} style={styles.empty} />;
  const message = {
    idle: 'Which song takes you back?',
    results: 'No songs found. Try the artist’s name too.',
    error: state.kind === 'error' ? state.message : '',
  }[state.kind];
  return <Text style={[type.subtitle, styles.empty, state.kind === 'error' && styles.error]}>{message}</Text>;
}

const useStyles = makeStyles(({ colors, type }) => ({
  safe: { flex: 1, backgroundColor: colors.bg },
  searchBox: { paddingHorizontal: space.lg, paddingTop: space.md },
  list: { paddingHorizontal: space.lg, paddingBottom: space.xl },
  empty: { marginTop: space.xl, textAlign: 'center' },
  error: { color: colors.danger },
}));
