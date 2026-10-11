import { Alert, Pressable, Text, View } from 'react-native';
import { PaperName } from '@/api/client';
import { LockIcon } from '@/components/icons';
import { Artwork } from '@/features/songs/Artwork';
import { StoryPaper } from '@/features/stories/StoryPaper';
import { StoryPhotoView } from '@/features/stories/StoryPhotoView';
import { makeStyles, paperNames, papers, radius, space, useTheme } from '@/theme';
import { ShareDraft } from './useShareDraft';

/** Step 3: a live preview of the card, the cover, and the paper it's printed on. */
export function LookStep({ draft }: { draft: ShareDraft }) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <View style={styles.preview}>
        <View style={styles.songRow}>
          <Artwork uri={draft.song.artworkUrl} size={40} />
          <View style={styles.songText}>
            <Text style={styles.songTitle} numberOfLines={1}>{draft.song.title}</Text>
            <Text style={styles.songArtist} numberOfLines={1}>{draft.song.artist}</Text>
          </View>
        </View>
        {draft.photo.hasPhoto
          ? <StoryPhotoView photo={draft.photo.saved} uri={draft.photo.local?.uri} />
          : null}
        <StoryPaper
          paper={draft.paper}
          body={draft.body.trim()}
          lyric={draft.lyric.trim() || null}
          momentSec={draft.momentSec}
          year={draft.year}
          bodyLines={6}
        />
      </View>

      <View style={styles.section}>
        <Text style={styles.label}>COVER</Text>
        <View style={styles.row}>
          <Option label="Album art" selected={!draft.photo.hasPhoto} onPress={draft.photo.chooseAlbumArt} />
          <Option label={draft.photo.hasPhoto ? 'Change photo' : 'Your photo'} selected={draft.photo.hasPhoto} onPress={draft.photo.choose} />
        </View>
        <Text style={styles.hint}>One photo per story. Photos are checked before anyone sees them.</Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.label}>PAPER</Text>
        <View style={styles.swatches}>
          {paperNames.map((name) => (
            <Swatch key={name} name={name} selected={draft.paper === name} onPick={() => draft.setPaper(name)} />
          ))}
        </View>
        <Text style={styles.hint}>Papers with a lock come with caraoke+.</Text>
      </View>
    </View>
  );
}

function Swatch({ name, selected, onPick }: { name: PaperName; selected: boolean; onPick: () => void }) {
  const styles = useStyles();
  const { colors } = useTheme();
  const paper = papers[name];
  const press = () =>
    paper.plus
      ? Alert.alert(`${paper.label} paper`, 'This one comes with caraoke+, which is coming soon.')
      : onPick();

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`${paper.label} paper${paper.plus ? ', caraoke+' : ''}`}
      accessibilityState={{ selected }}
      onPress={press}
      style={[styles.swatch, { backgroundColor: paper.bg, borderColor: selected ? colors.accent : paper.border }, selected && styles.swatchOn]}>
      {paper.plus ? <LockIcon color={paper.muted} /> : null}
    </Pressable>
  );
}

function Option({ label, selected, onPress }: { label: string; selected?: boolean; onPress: () => void }) {
  const styles = useStyles();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected: !!selected }}
      onPress={onPress}
      style={[styles.option, selected && styles.optionOn]}>
      <Text style={styles.optionText}>{label}</Text>
    </Pressable>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.lg },
  preview: { gap: space.sm },
  songRow: { flexDirection: 'row', alignItems: 'center', gap: space.sm },
  songText: { flex: 1 },
  songTitle: { ...type.body, fontWeight: '600' },
  songArtist: { ...type.hint },
  section: { gap: space.sm },
  label: { ...type.label },
  row: { flexDirection: 'row', gap: space.sm },
  option: {
    flex: 1,
    minHeight: 48,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },
  optionOn: { borderWidth: 2, borderColor: colors.accent },
  optionText: { ...type.body, fontSize: 15 },
  swatches: { flexDirection: 'row', gap: space.md, flexWrap: 'wrap' },
  swatch: { width: 44, height: 44, borderRadius: 22, borderWidth: 1, alignItems: 'center', justifyContent: 'center' },
  swatchOn: { borderWidth: 3 },
  hint: { ...type.hint },
}));
