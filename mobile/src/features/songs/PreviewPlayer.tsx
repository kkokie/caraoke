import { ActivityIndicator, Pressable, Text, View } from 'react-native';
import { PauseIcon, PlayIcon } from '@/components/icons';
import { formatDuration } from '@/lib/format';
import { makeStyles, radius, space, useTheme } from '@/theme';
import { usePreview } from './usePreview';

/** Play Apple's 30-second clip. Sits right above the "Listen on" links, as Apple's terms ask. */
export function PreviewPlayer({ songId }: { songId: number }) {
  const styles = useStyles();
  const { colors } = useTheme();
  const p = usePreview(songId);
  if (!p.available) return null;

  const pct = Math.min(1, p.position / p.duration);
  return (
    <View style={styles.wrap}>
      <View style={styles.row}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel={p.playing ? 'Pause preview' : 'Play preview'}
          onPress={p.toggle}
          style={({ pressed }) => [styles.button, pressed && styles.pressed]}>
          {p.loading
            ? <ActivityIndicator color={colors.accentText} />
            : p.playing ? <PauseIcon color={colors.accentText} /> : <PlayIcon color={colors.accentText} />}
        </Pressable>
        <View style={styles.track}>
          <View style={styles.bar}>
            <View style={[styles.fill, { width: `${pct * 100}%` }]} />
          </View>
          <View style={styles.times}>
            <Text style={styles.time}>Preview {formatDuration(p.position)}</Text>
            <Text style={styles.time}>{formatDuration(p.duration)}</Text>
          </View>
        </View>
      </View>
      <Text style={styles.attribution}>{p.attribution}</Text>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.xs },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: space.md,
    padding: space.md,
    borderRadius: radius.lg,
    backgroundColor: colors.surface,
  },
  button: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: colors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: { opacity: 0.8 },
  track: { flex: 1, gap: space.xs },
  bar: { height: 4, borderRadius: 2, backgroundColor: colors.border, overflow: 'hidden' },
  fill: { height: 4, backgroundColor: colors.accent },
  times: { flexDirection: 'row', justifyContent: 'space-between' },
  time: { ...type.mono, fontSize: 11 },
  attribution: { ...type.hint, fontSize: 11 },
}));
