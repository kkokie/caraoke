import { Alert, Linking, StyleSheet, View } from 'react-native';
import { ListenLinks } from '@/api/client';
import { Button } from '@/components/Button';
import { space } from '@/theme';

/** "Listen on…" buttons. We never play audio ourselves; we hand off to the user's app. */
export function ListenButtons({ links }: { links: ListenLinks }) {
  const options = [
    { label: 'Apple Music', url: links.appleMusic },
    { label: 'Spotify', url: links.spotify },
    { label: 'YouTube Music', url: links.youtubeMusic },
  ].filter((o): o is { label: string; url: string } => !!o.url);

  return (
    <View style={styles.row}>
      {options.map((o) => (
        <View key={o.label} style={styles.cell}>
          <Button label={o.label} variant="ghost" onPress={() => openUrl(o.url)} />
        </View>
      ))}
    </View>
  );
}

async function openUrl(url: string) {
  try {
    await Linking.openURL(url);
  } catch {
    Alert.alert('Couldn’t open link', url);
  }
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', flexWrap: 'wrap', gap: space.sm },
  cell: { flexGrow: 1, flexBasis: '30%' },
});
