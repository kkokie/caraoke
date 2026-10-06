import { StyleSheet, Text, View } from 'react-native';
import { PublicProfile } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { colors, radius, space, type } from '@/theme';

export function ProfileCard({ profile }: { profile: PublicProfile }) {
  return (
    <View style={styles.card}>
      <Avatar name={profile.displayName} size={56} />
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
  text: { flex: 1, gap: 2 },
  name: { ...type.body, fontWeight: '600' },
  handle: { ...type.hint, color: colors.textMuted },
  bio: { ...type.body, marginTop: space.sm, fontSize: 15 },
});
