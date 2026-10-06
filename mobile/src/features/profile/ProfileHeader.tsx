import { StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { ProfileView } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { Button } from '@/components/Button';
import { colors, space, type } from '@/theme';

/** Instagram-style header: avatar + stats on one row, then name, @handle, bio, action. */
export function ProfileHeader({ profile }: { profile: ProfileView }) {
  const { user, stats, me } = profile;
  return (
    <View style={styles.wrap}>
      <View style={styles.topRow}>
        <Avatar name={user.displayName} size={80} />
        <Stat value={stats.stories} label={stats.stories === 1 ? 'story' : 'stories'} />
        <Stat value={stats.felt} label="felt" />
      </View>

      <View style={styles.who}>
        <Text style={styles.name}>{user.displayName}</Text>
        <Text style={styles.handle}>@{user.handle}</Text>
        {user.bio ? <Text style={styles.bio}>{user.bio}</Text> : null}
      </View>

      {me ? <Button label="Edit profile" variant="ghost" onPress={() => router.push('/profile/edit')} /> : null}
    </View>
  );
}

function Stat({ value, label }: { value: number; label: string }) {
  return (
    <View style={styles.stat}>
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { gap: space.md, paddingBottom: space.md },
  topRow: { flexDirection: 'row', alignItems: 'center', gap: space.lg },
  stat: { flex: 1, alignItems: 'center' },
  statValue: { ...type.body, fontSize: 20, fontWeight: '700' },
  statLabel: { ...type.hint, color: colors.textMuted },
  who: { gap: 2 },
  name: { ...type.body, fontWeight: '700' },
  handle: { ...type.hint, color: colors.textMuted },
  bio: { ...type.body, fontSize: 15, marginTop: space.xs },
});
