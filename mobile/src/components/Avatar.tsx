import { useState } from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';
import { mediaUrl } from '@/api/client';
import { colors } from '@/theme';

type Props = { name: string; url?: string | null; size?: number };

/** Profile photo in a circle; falls back to the name's initial when there's no photo (or it fails to load). */
export function Avatar({ name, url, size = 32 }: Props) {
  const [failed, setFailed] = useState(false);
  const uri = mediaUrl(url);
  const shape = { width: size, height: size, borderRadius: size / 2 };

  if (uri && !failed) {
    return <Image source={{ uri }} style={[styles.photo, shape]} onError={() => setFailed(true)} />;
  }
  return (
    <View style={[styles.circle, shape]}>
      <Text style={[styles.initial, { fontSize: size * 0.44 }]}>{name.charAt(0).toUpperCase()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  photo: { backgroundColor: colors.surface },
  circle: { backgroundColor: colors.accent, alignItems: 'center', justifyContent: 'center' },
  initial: { fontWeight: '700', color: colors.accentText },
});
