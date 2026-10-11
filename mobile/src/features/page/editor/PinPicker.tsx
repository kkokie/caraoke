import { useEffect, useState } from 'react';
import { Image, Pressable, ScrollView, Text, View } from 'react-native';
import { api, StoryTile } from '@/api/client';
import { makeStyles, radius, space } from '@/theme';

/** Your stories, sideways; tap one to pin it, tap it again to unpin. */
export function PinPicker({ handle, pinned, onChange }: {
  handle: string;
  pinned: StoryTile | null;
  onChange: (t: StoryTile | null) => void;
}) {
  const styles = useStyles();
  const [tiles, setTiles] = useState<StoryTile[]>([]);

  useEffect(() => {
    api.getProfileStories(handle).then((p) => setTiles(p.items)).catch(() => setTiles([]));
  }, [handle]);

  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>PINNED STORY</Text>
      {tiles.length === 0 ? <Text style={styles.hint}>Share a story first, then pin it here.</Text> : null}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.row}>
        {tiles.map((t) => {
          const on = pinned?.storyId === t.storyId;
          return (
            <Pressable
              key={t.storyId}
              accessibilityRole="radio"
              accessibilityState={{ selected: on }}
              accessibilityLabel={`Pin your story about ${t.songTitle ?? 'a song'}`}
              onPress={() => onChange(on ? null : t)}
              style={[styles.tile, on && styles.tileOn]}>
              {t.coverUrl
                ? <Image source={{ uri: t.coverUrl }} style={styles.cover} accessibilityIgnoresInvertColors />
                : <View style={styles.cover} />}
              <Text style={styles.song} numberOfLines={1}>{t.songTitle ?? ''}</Text>
            </Pressable>
          );
        })}
      </ScrollView>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  label: { ...type.label },
  hint: { ...type.hint },
  row: { gap: space.sm },
  tile: { width: 84, gap: space.xs, padding: 2, borderRadius: radius.md, borderWidth: 2, borderColor: 'transparent' },
  tileOn: { borderColor: colors.accent },
  cover: { width: 76, height: 76, borderRadius: radius.md - 2, backgroundColor: colors.border },
  song: { ...type.hint, fontSize: 12 },
}));
