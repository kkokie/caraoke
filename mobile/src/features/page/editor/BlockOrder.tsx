import { Pressable, Switch, Text, View } from 'react-native';
import { PageBlockName } from '@/api/client';
import { makeStyles, space, useTheme } from '@/theme';

const NAMES: Record<PageBlockName, string> = {
  PAGE_SONG: 'Page song',
  PINNED_STORY: 'Pinned story',
  LIFE_IN_SONGS: 'My life in songs',
  RAISED_ME: 'Songs that raised me',
};

/** Which sections show, and in what order (arrows move them; no drag needed). */
export function BlockOrder({ order, shown, onToggle, onMove }: {
  order: PageBlockName[];
  shown: PageBlockName[];
  onToggle: (b: PageBlockName) => void;
  onMove: (b: PageBlockName, by: -1 | 1) => void;
}) {
  const styles = useStyles();
  const { colors } = useTheme();
  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>SECTIONS</Text>
      {order.map((b, i) => (
        <View key={b} style={styles.row}>
          <Switch
            value={shown.includes(b)}
            onValueChange={() => onToggle(b)}
            trackColor={{ true: colors.accent, false: colors.border }}
            accessibilityLabel={`Show ${NAMES[b]}`}
          />
          <Text style={[styles.name, !shown.includes(b) && styles.off]}>{NAMES[b]}</Text>
          <Arrow label="Move up" glyph="↑" disabled={i === 0} onPress={() => onMove(b, -1)} />
          <Arrow label="Move down" glyph="↓" disabled={i === order.length - 1} onPress={() => onMove(b, 1)} />
        </View>
      ))}
    </View>
  );
}

function Arrow({ label, glyph, disabled, onPress }: { label: string; glyph: string; disabled: boolean; onPress: () => void }) {
  const styles = useStyles();
  return (
    <Pressable accessibilityRole="button" accessibilityLabel={label} disabled={disabled} onPress={onPress} hitSlop={8}>
      <Text style={[styles.arrow, disabled && styles.off]}>{glyph}</Text>
    </Pressable>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  label: { ...type.label },
  row: { flexDirection: 'row', alignItems: 'center', gap: space.md },
  name: { ...type.body, flex: 1 },
  off: { opacity: 0.35 },
  arrow: { fontSize: 20, color: colors.accent, paddingHorizontal: space.xs },
}));
