import { useState } from 'react';
import { Text, View } from 'react-native';
import { SongSummary } from '@/api/client';
import { Button } from '@/components/Button';
import { Artwork } from '@/features/songs/Artwork';
import { SongPicker } from '@/features/songs/SongPicker';
import { makeStyles, space } from '@/theme';

/** The song that plays (on tap) when someone visits your page. */
export function PageSongSection({ song, onChange }: { song: SongSummary | null; onChange: (s: SongSummary | null) => void }) {
  const styles = useStyles();
  const [picking, setPicking] = useState(!song);

  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>PAGE SONG</Text>
      {song && !picking ? (
        <View style={styles.row}>
          <Artwork uri={song.artworkUrl} size={48} />
          <View style={styles.text}>
            <Text style={styles.title} numberOfLines={1}>{song.title}</Text>
            <Text style={styles.hint} numberOfLines={1}>{song.artist}</Text>
          </View>
          <View style={styles.button}><Button label="Change" variant="ghost" onPress={() => setPicking(true)} /></View>
        </View>
      ) : (
        <SongPicker label="Your page song" onPick={(s) => { onChange(s); setPicking(false); }} />
      )}
      {song ? <Button label="No page song" variant="ghost" onPress={() => { onChange(null); setPicking(true); }} /> : null}
    </View>
  );
}

const useStyles = makeStyles(({ type }) => ({
  wrap: { gap: space.sm },
  label: { ...type.label },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  text: { flex: 1 },
  title: { ...type.body, fontWeight: '600' },
  hint: { ...type.hint },
  button: { width: 110 },
}));
