import { Image, StyleSheet, Text, View } from 'react-native';
import { colors } from '@/theme';

type Props = { uri: string | null; size: number; radius?: number };

/** Album art with a warm placeholder when a track has no artwork. */
export function Artwork({ uri, size, radius = 8 }: Props) {
  const box = { width: size, height: size, borderRadius: radius };
  if (!uri) {
    return (
      <View style={[styles.placeholder, box]}>
        <Text style={[styles.note, { fontSize: size * 0.4 }]}>♪</Text>
      </View>
    );
  }
  return <Image source={{ uri }} style={[styles.image, box]} accessibilityIgnoresInvertColors />;
}

const styles = StyleSheet.create({
  image: { backgroundColor: colors.surface },
  placeholder: { backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center' },
  note: { color: colors.textMuted },
});
