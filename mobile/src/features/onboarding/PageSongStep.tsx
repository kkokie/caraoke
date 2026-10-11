import { useState } from 'react';
import { Alert, Text, View } from 'react-native';
import { api, Song } from '@/api/client';
import { Button } from '@/components/Button';
import { Screen } from '@/components/Screen';
import { Artwork } from '@/features/songs/Artwork';
import { SongPicker } from '@/features/songs/SongPicker';
import { makeStyles, space, useTheme } from '@/theme';

/** Onboarding step 2: the song that plays when someone visits your page. Skippable. */
export function PageSongStep({ onDone }: { onDone: () => void }) {
  const styles = useStyles();
  const { type } = useTheme();
  const [song, setSong] = useState<Song | null>(null);
  const [saving, setSaving] = useState(false);

  async function save() {
    if (!song) return onDone();
    setSaving(true);
    try {
      await api.setPageSong(song.id);
      onDone();
    } catch (e) {
      Alert.alert('Couldn’t save your page song', e instanceof Error ? e.message : String(e));
      setSaving(false);
    }
  }

  return (
    <Screen>
      <View style={styles.header}>
        <Text style={type.label}>STEP 2 OF 2</Text>
        <Text style={type.title}>Your page{'\n'}song.</Text>
        <Text style={type.subtitle}>The song people hear when they visit your page. You can change it any time.</Text>
      </View>

      {song ? (
        <View style={styles.picked}>
          <Artwork uri={song.artworkUrl} size={64} />
          <View style={styles.flex}>
            <Text style={styles.title} numberOfLines={1}>{song.title}</Text>
            <Text style={type.hint} numberOfLines={1}>{song.artist}</Text>
          </View>
          <View style={styles.change}><Button label="Change" variant="ghost" onPress={() => setSong(null)} /></View>
        </View>
      ) : (
        <SongPicker label="Find your song" onPick={setSong} />
      )}

      <View style={styles.footer}>
        <Button label={song ? 'Open caraoke' : 'Skip for now'} variant={song ? 'primary' : 'ghost'} onPress={save} loading={saving} />
      </View>
    </Screen>
  );
}

const useStyles = makeStyles(({ type }) => ({
  header: { gap: space.sm, marginTop: space.xl },
  picked: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  flex: { flex: 1 },
  title: { ...type.body, fontWeight: '600' },
  change: { width: 110 },
  footer: { marginTop: 'auto', gap: space.sm },
}));
