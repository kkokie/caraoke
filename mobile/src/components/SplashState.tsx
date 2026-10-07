import { ActivityIndicator, Text, View } from 'react-native';
import { makeStyles, space, useTheme } from '@/theme';
import { Button } from './Button';

type Props = { error?: string; onRetry?: () => void };

/** Full-screen loading or "can't reach the API" state, shown before routing kicks in. */
export function SplashState({ error, onRetry }: Props) {
  const styles = useStyles();
  const { colors, type } = useTheme();
  return (
    <View style={styles.wrap}>
      <Text style={styles.brand}>caraoke</Text>
      {error ? (
        <View style={styles.errorBox}>
          <Text style={type.subtitle}>Can’t reach the server.</Text>
          <Text style={[type.hint, styles.detail]}>{error}</Text>
          {onRetry ? <Button label="Try again" variant="ghost" onPress={onRetry} /> : null}
        </View>
      ) : (
        <ActivityIndicator color={colors.accent} />
      )}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg, gap: space.lg },
  brand: { ...type.title, color: colors.accent, letterSpacing: -0.5 },
  errorBox: { alignSelf: 'stretch', gap: space.md, alignItems: 'center' },
  detail: { color: colors.textMuted, textAlign: 'center' },
}));
