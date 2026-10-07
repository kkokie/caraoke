import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { ProfileView } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { Button } from '@/components/Button';
import { PhotoViewer } from '@/components/PhotoViewer';
import { useFollow } from '@/features/follow/useFollow';
import { colors, space, type } from '@/theme';

/**
 * Instagram-style header: avatar + stories / followers / following on one row,
 * then name, @handle, felt count, bio, and Edit profile (you) or Follow (anyone else).
 */
export function ProfileHeader({ profile }: { profile: ProfileView }) {
  const { user, stats, me } = profile;
  const follow = useFollow(profile);
  const [viewing, setViewing] = useState(false);
  const openList = (list: 'followers' | 'following') =>
    router.push({ pathname: `/user/[handle]/${list}`, params: { handle: user.handle } });

  return (
    <View style={styles.wrap}>
      <View style={styles.topRow}>
        <Pressable
          onPress={() => setViewing(true)}
          disabled={!user.avatarUrl}
          accessibilityRole="imagebutton"
          accessibilityLabel={`View ${user.displayName}'s photo`}>
          <Avatar name={user.displayName} url={user.avatarUrl} size={80} />
        </Pressable>
        <Stat value={stats.stories} label={stats.stories === 1 ? 'story' : 'stories'} />
        <Stat value={follow.followers} label={follow.followers === 1 ? 'follower' : 'followers'} onPress={() => openList('followers')} />
        <Stat value={profile.social.following} label="following" onPress={() => openList('following')} />
      </View>

      <View style={styles.who}>
        <Text style={styles.name}>{user.displayName}</Text>
        <Text style={styles.handle}>@{user.handle}</Text>
        {stats.felt > 0 ? (
          <Text style={styles.felt}>♥ People felt {me ? 'your' : 'their'} stories {stats.felt.toLocaleString()} {stats.felt === 1 ? 'time' : 'times'}</Text>
        ) : null}
        {user.bio ? <Text style={styles.bio}>{user.bio}</Text> : null}
      </View>

      {me
        ? <Button label="Edit profile" variant="ghost" onPress={() => router.push('/profile/edit')} />
        : <Button label={follow.following ? 'Following' : 'Follow'} variant={follow.following ? 'ghost' : 'primary'} onPress={follow.toggle} />}

      <PhotoViewer url={user.avatarUrl} visible={viewing} onClose={() => setViewing(false)} label={user.displayName} />
    </View>
  );
}

function Stat({ value, label, onPress }: { value: number; label: string; onPress?: () => void }) {
  const content = (
    <>
      <Text style={styles.statValue}>{value.toLocaleString()}</Text>
      <Text style={styles.statLabel}>{label}</Text>
    </>
  );
  if (!onPress) return <View style={styles.stat}>{content}</View>;
  return (
    <Pressable accessibilityRole="link" accessibilityLabel={`${value} ${label}`} onPress={onPress} style={({ pressed }) => [styles.stat, pressed && styles.pressed]}>
      {content}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  wrap: { gap: space.md, paddingBottom: space.md },
  topRow: { flexDirection: 'row', alignItems: 'center', gap: space.sm },
  stat: { flex: 1, alignItems: 'center' },
  pressed: { opacity: 0.6 },
  statValue: { ...type.body, fontSize: 19, fontWeight: '700' },
  statLabel: { ...type.hint, color: colors.textMuted },
  who: { gap: 2 },
  name: { ...type.body, fontWeight: '700' },
  handle: { ...type.hint, color: colors.textMuted },
  felt: { ...type.hint, color: colors.accent, marginTop: space.xs },
  bio: { ...type.body, fontSize: 15, marginTop: space.xs },
});
