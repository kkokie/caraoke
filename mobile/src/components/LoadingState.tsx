import { ReactNode } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { colors, space, type } from '@/theme';

/** Full-screen spinner or error, for screens that load something before rendering. */
export function LoadingState({ error }: { error?: string | null }) {
  return (
    <Centered>
      {error
        ? <Text style={[type.subtitle, styles.error]}>{error}</Text>
        : <ActivityIndicator color={colors.accent} />}
    </Centered>
  );
}

export function Centered({ children }: { children: ReactNode }) {
  return <View style={styles.centered}>{children}</View>;
}

const styles = StyleSheet.create({
  centered: { flex: 1, backgroundColor: colors.bg, alignItems: 'center', justifyContent: 'center', padding: space.lg, gap: space.md },
  error: { color: colors.danger, textAlign: 'center' },
});
