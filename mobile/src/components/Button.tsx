import { ActivityIndicator, Pressable, Text } from 'react-native';
import { makeStyles, radius, space, useTheme } from '@/theme';

type Props = {
  label: string;
  onPress: () => void;
  disabled?: boolean;
  loading?: boolean;
  variant?: 'primary' | 'ghost';
};

export function Button({ label, onPress, disabled, loading, variant = 'primary' }: Props) {
  const styles = useStyles();
  const { colors } = useTheme();
  const isPrimary = variant === 'primary';
  const inactive = disabled || loading;

  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      disabled={inactive}
      style={({ pressed }) => [
        styles.base,
        isPrimary ? styles.primary : styles.ghost,
        inactive && styles.inactive,
        pressed && styles.pressed,
      ]}>
      {loading
        ? <ActivityIndicator color={isPrimary ? colors.accentText : colors.text} />
        : <Text style={[styles.label, isPrimary ? styles.primaryLabel : styles.ghostLabel]}>{label}</Text>}
    </Pressable>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  base: { minHeight: 52, borderRadius: radius.md, alignItems: 'center', justifyContent: 'center', paddingHorizontal: space.md },
  primary: { backgroundColor: colors.accent },
  ghost: { borderWidth: 1, borderColor: colors.border },
  inactive: { opacity: 0.4 },
  pressed: { opacity: 0.8 },
  label: { fontSize: 16, fontWeight: '600' },
  primaryLabel: { color: colors.accentText },
  ghostLabel: { color: colors.text },
}));
