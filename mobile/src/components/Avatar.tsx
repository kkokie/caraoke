import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/theme';

/** Initial-in-a-circle avatar until we support photos. */
export function Avatar({ name, size = 32 }: { name: string; size?: number }) {
  return (
    <View style={[styles.circle, { width: size, height: size, borderRadius: size / 2 }]}>
      <Text style={[styles.initial, { fontSize: size * 0.44 }]}>{name.charAt(0).toUpperCase()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  circle: { backgroundColor: colors.accent, alignItems: 'center', justifyContent: 'center' },
  initial: { fontWeight: '700', color: colors.accentText },
});
