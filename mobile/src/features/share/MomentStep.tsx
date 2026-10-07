import { useEffect, useMemo, useRef } from 'react';
import { FlatList, Pressable, Text, View } from 'react-native';
import { formatDuration } from '@/lib/format';
import { makeStyles, radius, space } from '@/theme';
import { MomentScrubber } from './MomentScrubber';
import { MIN_YEAR, ShareDraft } from './useShareDraft';

/** Step 1: the second that gets you (or the whole song) and the year the memory is from. */
type Props = { draft: ShareDraft; onScrubbing: (active: boolean) => void };

export function MomentStep({ draft, onScrubbing }: Props) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <Text style={styles.title}>
        Where does it <Text style={styles.accent}>hit you?</Text>
      </Text>
      <MomentPicker draft={draft} onScrubbing={onScrubbing} />
      <YearPicker year={draft.year} onChange={draft.setYear} />
    </View>
  );
}

function MomentPicker({ draft, onScrubbing }: Props) {
  const styles = useStyles();
  const duration = draft.song.durationSec;
  const { momentSec, setMomentSec } = draft;
  const nudge = (d: number) => setMomentSec(clamp((momentSec ?? 0) + d, 0, duration ?? 0));

  // Without a known length (rare), there's nothing to scrub along: the whole song it is.
  if (!duration) return <Text style={styles.hint}>This one’s about the whole song.</Text>;

  return (
    <View style={styles.moment}>
      <Text style={[styles.time, momentSec == null && styles.timeEmpty]}>
        {momentSec == null ? 'whole song' : formatDuration(momentSec)}
      </Text>
      <View style={styles.full}>
        <MomentScrubber value={momentSec ?? 0} durationSec={duration} onChange={setMomentSec} onScrubbing={onScrubbing} />
        <View style={styles.ends}>
          <Text style={styles.endLabel}>0:00</Text>
          <Text style={styles.endLabel}>{formatDuration(duration)}</Text>
        </View>
      </View>
      <View style={styles.row}>
        <Chip label="−5s" onPress={() => nudge(-5)} />
        <Chip label="+5s" onPress={() => nudge(5)} />
        <Chip label="Whole song" selected={momentSec == null} onPress={() => setMomentSec(null)} />
      </View>
      <Text style={styles.hint}>Drag to the part that gets you. The preview is Apple’s clip; your moment is a bookmark in the full song.</Text>
    </View>
  );
}

/** Newest first, so recent memories are a short scroll; "Not sure" leaves it off. */
function YearPicker({ year, onChange }: { year: number | null; onChange: (y: number | null) => void }) {
  const styles = useStyles();
  const years = useMemo(() => {
    const now = new Date().getFullYear();
    return Array.from({ length: now - MIN_YEAR + 1 }, (_, i) => now - i);
  }, []);
  const list = useRef<FlatList<number>>(null);

  // Editing an older story: bring its year into view once, instead of leaving it far off-screen
  const initialIndex = year == null ? -1 : years.indexOf(year);
  useEffect(() => {
    if (initialIndex < 0) return;
    const t = setTimeout(() => list.current?.scrollToIndex({ index: initialIndex, viewPosition: 0.4, animated: false }), 0);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <View style={styles.yearBlock}>
      <Text style={styles.label}>WHAT YEAR IS THIS MEMORY FROM?</Text>
      <FlatList
        ref={list}
        horizontal
        onScrollToIndexFailed={(info) =>
          list.current?.scrollToOffset({ offset: info.averageItemLength * info.index, animated: false })}
        data={years}
        keyExtractor={String}
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.years}
        ListHeaderComponent={<Chip label="Not sure" selected={year == null} onPress={() => onChange(null)} />}
        renderItem={({ item }) => (
          <Pressable
            accessibilityRole="button"
            accessibilityState={{ selected: item === year }}
            onPress={() => onChange(item)}
            style={styles.yearHit}>
            <Text style={[styles.year, item === year && styles.yearOn]}>{item}</Text>
          </Pressable>
        )}
      />
    </View>
  );
}

function Chip({ label, selected, onPress }: { label: string; selected?: boolean; onPress: () => void }) {
  const styles = useStyles();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected: !!selected }}
      onPress={onPress}
      style={({ pressed }) => [styles.chip, selected && styles.chipOn, pressed && styles.pressed]}>
      <Text style={[styles.chipText, selected && styles.chipTextOn]}>{label}</Text>
    </Pressable>
  );
}

const clamp = (n: number, lo: number, hi: number) => Math.min(hi, Math.max(lo, n));

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.xl },
  title: { ...type.title },
  accent: { fontFamily: type.lyric.fontFamily, color: colors.accent },
  moment: { alignItems: 'center', gap: space.md },
  time: { ...type.mono, fontSize: 56, lineHeight: 64, color: colors.accent },
  timeEmpty: { fontSize: 32, lineHeight: 64 },
  full: { alignSelf: 'stretch' },
  ends: { flexDirection: 'row', justifyContent: 'space-between' },
  endLabel: { ...type.mono, fontSize: 11 },
  row: { flexDirection: 'row', gap: space.sm },
  hint: { ...type.hint, textAlign: 'center' },
  yearBlock: { gap: space.sm },
  label: { ...type.label },
  years: { alignItems: 'center', gap: space.sm, paddingRight: space.lg },
  yearHit: { paddingHorizontal: space.xs, minHeight: 44, justifyContent: 'center' },
  year: { fontFamily: type.story.fontFamily, fontSize: 24, color: colors.textMuted },
  yearOn: { fontSize: 38, color: colors.accent },
  chip: {
    minHeight: 40,
    paddingHorizontal: space.md,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.border,
    justifyContent: 'center',
  },
  chipOn: { borderColor: colors.accent, backgroundColor: colors.accent },
  chipText: { ...type.mono, fontSize: 14, color: colors.text },
  chipTextOn: { color: colors.accentText },
  pressed: { opacity: 0.7 },
}));
