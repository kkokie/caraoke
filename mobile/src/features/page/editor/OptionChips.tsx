import { Pressable, Text, View } from 'react-native';
import { makeStyles, radius, space } from '@/theme';

type Option<T extends string> = { name: T; label: string; swatch?: string };

/** One pick from a short curated list, as chips. */
export function OptionChips<T extends string>({ label, options, value, onChange }: {
  label: string;
  options: Option<T>[];
  value: T;
  onChange: (v: T) => void;
}) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.row}>
        {options.map((o) => {
          const on = o.name === value;
          return (
            <Pressable
              key={o.name}
              accessibilityRole="radio"
              accessibilityState={{ selected: on }}
              onPress={() => onChange(o.name)}
              style={[styles.chip, on && styles.chipOn]}>
              {o.swatch ? <View style={[styles.swatch, { backgroundColor: o.swatch }]} /> : null}
              <Text style={[styles.text, on && styles.textOn]}>{o.label}</Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  label: { ...type.label },
  row: { flexDirection: 'row', flexWrap: 'wrap', gap: space.sm },
  chip: {
    flexDirection: 'row', alignItems: 'center', gap: space.xs,
    paddingHorizontal: space.md, paddingVertical: space.sm,
    borderRadius: radius.pill, borderWidth: 1, borderColor: colors.border,
  },
  chipOn: { borderColor: colors.accent, backgroundColor: colors.surface },
  swatch: { width: 14, height: 14, borderRadius: 7, borderWidth: 1, borderColor: colors.border },
  text: { ...type.hint, color: colors.text },
  textOn: { color: colors.accent, fontWeight: '600' },
}));
