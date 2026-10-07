import { ReactNode } from 'react';
import { ActivityIndicator, Text, View } from 'react-native';
import { makeStyles, space, useTheme } from '@/theme';

/** Full-screen spinner or error, for screens that load something before rendering. */
export function LoadingState({ error }: { error?: string | null }) {
  const styles = useStyles();
  const { colors, type } = useTheme();
  return (
    <Centered>
      {error
        ? <Text style={[type.subtitle, styles.error]}>{error}</Text>
        : <ActivityIndicator color={colors.accent} />}
    </Centered>
  );
}

export function Centered({ children }: { children: ReactNode }) {
  const styles = useStyles();
  return <View style={styles.centered}>{children}</View>;
}

const useStyles = makeStyles(({ colors, type }) => ({
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg, gap: space.md },
  error: { color: colors.danger, textAlign: 'center' },
}));
