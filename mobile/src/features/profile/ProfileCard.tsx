import { StyleSheet, Text, View } from 'react-native';
import { PublicProfile } from '@/api/client';
import { colors, radius, space, type } from '@/theme';

export function ProfileCard({ profile }: { profile: PublicProfile }) {
  return (
    <View style={styles.card}>
      <View style={styles.avatar}>
        <Text style={styles.initial}>{profile.displayName.charAt(0).toUpperCase()}</Text>
      </View>
      <View style={styles.text}>
        <Text style={styles.name}>{profile.displayName}</Text>
        <Text style={styles.handle}>@{profile.handle}</Text>
        {profile.bio ? <Text style={styles.bio}>{profile.bio}</Text> : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    gap: space.md,
    padding: space.md,
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
  },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: colors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  initial: { fontSize: 24, fontWeight: '700', color: colors.accentText },
  text: { flex: 1, gap: 2 },
  name: { ...type.body, fontWeight: '600' },
  handle: { ...type.hint, color: colors.textMuted },
  bio: { ...type.body, marginTop: space.sm, fontSize: 15 },
});
