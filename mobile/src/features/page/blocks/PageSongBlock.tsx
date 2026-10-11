import { Text, View } from 'react-native';
import { Song } from '@/api/client';
import { Artwork } from '@/features/songs/Artwork';
import { ListenButtons } from '@/features/songs/ListenButtons';
import { PreviewPlayer } from '@/features/songs/PreviewPlayer';
import { makeStyles, radius, space } from '@/theme';
import { BlockTitle } from './BlockTitle';

/**
 * Their page song. Tap to play Apple's preview (never autoplay: the preview is promotional,
 * and it sits right above "Listen on", as Apple's terms ask).
 */
export function PageSongBlock({ song }: { song: Song }) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <BlockTitle>PAGE SONG</BlockTitle>
      <View style={styles.row}>
        <Artwork uri={song.artworkUrl} size={56} radius={radius.md} />
        <View style={styles.text}>
          <Text style={styles.title} numberOfLines={1}>{song.title}</Text>
          <Text style={styles.artist} numberOfLines={1}>{song.artist}</Text>
        </View>
      </View>
      <PreviewPlayer songId={song.id} />
      <ListenButtons links={song.listen} />
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  text: { flex: 1 },
  title: { ...type.title, fontSize: 22, lineHeight: 26 },
  artist: { ...type.hint, color: colors.textMuted },
}));
