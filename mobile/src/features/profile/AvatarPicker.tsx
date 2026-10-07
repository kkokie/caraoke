import { ActivityIndicator, Pressable, Text, View } from 'react-native';
import { PublicProfile } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { PhotoViewer } from '@/components/PhotoViewer';
import { makeStyles, space, useTheme } from '@/theme';
import { useAvatarPhoto } from './useAvatarPhoto';

/** Tappable profile photo at the top of Edit profile. */
export function AvatarPicker({ profile }: { profile: PublicProfile }) {
  const styles = useStyles();
  const { colors, type } = useTheme();
  const photo = useAvatarPhoto(profile);

  return (
    <Pressable onPress={photo.openMenu} style={styles.wrap} accessibilityRole="button" accessibilityLabel="Change profile photo">
      <View>
        <Avatar name={profile.displayName} url={profile.avatarUrl} size={96} />
        {photo.busy ? (
          <View style={styles.overlay}>
            <ActivityIndicator color={colors.text} />
          </View>
        ) : null}
      </View>
      <Text style={[type.hint, styles.link]}>{photo.hasPhoto ? 'Edit photo' : 'Add photo'}</Text>
      <PhotoViewer url={profile.avatarUrl} visible={photo.viewing} onClose={photo.closeViewer} label={profile.displayName} />
    </Pressable>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { alignItems: 'center', gap: space.sm, paddingVertical: space.md },
  overlay: {
    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
    borderRadius: 48,
    backgroundColor: 'rgba(0,0,0,0.45)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  link: { color: colors.accent, fontWeight: '600' },
}));
