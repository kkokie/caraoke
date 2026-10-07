import { Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { ProfileView } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { Button } from '@/components/Button';
import { useFollow } from '@/features/follow/useFollow';
import { makeStyles, space } from '@/theme';

/**
 * Instagram-style header: avatar + stories / followers / following on one row,
 * then name, @handle, felt count, bio, and Edit profile (you) or Follow (anyone else).
 */
export function ProfileHeader({ profile }: { profile: ProfileView }) {
  const styles = useStyles();
  const { user, stats, me } = profile;
  const follow = useFollow(profile);
  const openList = (list: 'followers' | 'following') =>
    router.push({ pathname: `/user/[handle]/${list}`, params: { handle: user.handle } });

  return (
    <View style={styles.wrap}>
      <View style={styles.topRow}>
        <Avatar name={user.displayName} url={user.avatarUrl} size={80} />
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
    </View>
  );
}

function Stat({ value, label, onPress }: { value: number; label: string; onPress?: () => void }) {
  const styles = useStyles();
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

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.md, paddingBottom: space.md },
  topRow: { flexDirection: 'row', alignItems: 'center', gap: space.sm },
  stat: { flex: 1, alignItems: 'center' },
  pressed: { opacity: 0.6 },
  statValue: { ...type.mono, fontSize: 18, color: colors.text },
  statLabel: { ...type.hint, color: colors.textMuted },
  who: { gap: 2 },
  name: { ...type.title, fontSize: 30, lineHeight: 34 },
  handle: { ...type.hint, color: colors.textMuted },
  felt: { ...type.hint, color: colors.accent, marginTop: space.xs },
  bio: { ...type.lyric, fontSize: 18, lineHeight: 24, marginTop: space.xs },
}));
