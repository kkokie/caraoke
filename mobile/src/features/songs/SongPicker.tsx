import { useState } from 'react';
import { ActivityIndicator, Alert, Text, View } from 'react-native';
import { api, Song, SongSearchResult } from '@/api/client';
import { TextField } from '@/components/TextField';
import { makeStyles, space, useTheme } from '@/theme';
import { SongRow } from './SongRow';
import { useSongSearch } from './useSongSearch';

const MAX_RESULTS = 8;

/**
 * Search and pick one song, inline (no screen change). Picking resolves it into OUR song first,
 * so callers always get a real song id.
 */
export function SongPicker({ label = 'Find a song', onPick }: { label?: string; onPick: (song: Song) => void }) {
  const styles = useStyles();
  const { colors } = useTheme();
  const [query, setQuery] = useState('');
  const [resolving, setResolving] = useState<string | null>(null);
  const search = useSongSearch(query);

  async function pick(result: SongSearchResult) {
    if (resolving) return;
    setResolving(result.appleId);
    try {
      onPick(await api.resolveSong(result.appleId));
      setQuery('');
    } catch (e) {
      Alert.alert('Couldn’t add that song', e instanceof Error ? e.message : String(e));
    } finally {
      setResolving(null);
    }
  }

  return (
    <View style={styles.wrap}>
      <TextField label={label} value={query} onChangeText={setQuery} placeholder="Song or artist" autoCorrect={false} />
      {search.kind === 'searching' ? <ActivityIndicator color={colors.accent} /> : null}
      {search.kind === 'error' ? <Text style={styles.error}>{search.message}</Text> : null}
      {search.kind === 'results' && search.items.length === 0 ? <Text style={styles.hint}>No songs found.</Text> : null}
      {search.kind === 'results'
        ? search.items.slice(0, MAX_RESULTS).map((r) => (
            <SongRow key={r.appleId} song={r} loading={resolving === r.appleId} onPress={() => pick(r)} />
          ))
        : null}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.xs },
  hint: { ...type.hint },
  error: { ...type.hint, color: colors.danger },
}));
