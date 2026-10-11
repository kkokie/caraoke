import { Image } from 'react-native';
import { mediaUrl, StoryPhoto } from '@/api/client';
import { makeStyles, radius } from '@/theme';

const DEFAULT_RATIO = 4 / 5;               // portrait, until we know the real size
const MIN_RATIO = 3 / 4, MAX_RATIO = 16 / 9; // keep very tall or very wide photos tidy in a feed

/** A story's photo, sized from its known shape so the feed doesn't jump when it loads. */
export function StoryPhotoView({ photo, uri }: { photo?: StoryPhoto | null; uri?: string }) {
  const styles = useStyles();
  const source = uri ?? mediaUrl(photo?.url);
  if (!source) return null;
  const ratio = photo?.width && photo?.height ? photo.width / photo.height : DEFAULT_RATIO;
  return (
    <Image
      source={{ uri: source }}
      style={[styles.photo, { aspectRatio: Math.min(MAX_RATIO, Math.max(MIN_RATIO, ratio)) }]}
      resizeMode="cover"
      accessibilityIgnoresInvertColors
      accessibilityLabel="Photo with this story"
    />
  );
}

const useStyles = makeStyles(({ colors }) => ({
  photo: { width: '100%', borderRadius: radius.md, backgroundColor: colors.surface },
}));
