import { useEffect, useState } from 'react';
import { LayoutChangeEvent, Pressable, Text, View } from 'react-native';
import { api, MomentCount } from '@/api/client';
import { formatDuration } from '@/lib/format';
import { makeStyles, space } from '@/theme';

/**
 * The song as a line, with a dot wherever people pinned a moment (bigger = more stories).
 * Tap a dot to see when it is and how many stories are there.
 */
export function MomentsTimeline({ songId, durationSec }: { songId: number; durationSec: number | null }) {
  const styles = useStyles();
  const [moments, setMoments] = useState<MomentCount[]>([]);
  const [width, setWidth] = useState(0);
  const [picked, setPicked] = useState<MomentCount | null>(null);

  useEffect(() => {
    api.getMoments(songId).then(setMoments).catch(() => setMoments([]));
  }, [songId]);

  if (!durationSec || moments.length === 0) return null;
  const busiest = moments.reduce((a, b) => (b.stories > a.stories ? b : a));
  const shown = picked ?? busiest;

  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>MOMENTS PEOPLE PINNED</Text>
      <View style={styles.line} onLayout={(e: LayoutChangeEvent) => setWidth(e.nativeEvent.layout.width)}>
        <View style={styles.rule} />
        {width > 0 && moments.map((m) => {
          const size = 8 + Math.min(14, (m.stories / busiest.stories) * 14);
          const left = Math.min(width - size, Math.max(0, (m.sec / durationSec) * width - size / 2));
          const on = m.sec === shown.sec;
          return (
            <Pressable
              key={m.sec}
              accessibilityRole="button"
              accessibilityLabel={`${formatDuration(m.sec)}, ${m.stories} ${m.stories === 1 ? 'story' : 'stories'}`}
              hitSlop={10}
              onPress={() => setPicked(m)}
              style={[styles.dot, on && styles.dotOn, { width: size, height: size, borderRadius: size / 2, left, top: 14 - size / 2 }]}
            />
          );
        })}
      </View>
      <View style={styles.ends}>
        <Text style={styles.end}>0:00</Text>
        <Text style={styles.picked}>
          {formatDuration(shown.sec)} · {shown.stories} {shown.stories === 1 ? 'story' : 'stories'}
        </Text>
        <Text style={styles.end}>{formatDuration(durationSec)}</Text>
      </View>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  label: { ...type.label },
  line: { height: 28, justifyContent: 'center' },
  rule: { height: 2, backgroundColor: colors.border },
  dot: { position: 'absolute', backgroundColor: colors.dot },
  dotOn: { backgroundColor: colors.accent },
  ends: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  end: { ...type.mono, fontSize: 11 },
  picked: { ...type.mono, fontSize: 12, color: colors.accent },
}));
