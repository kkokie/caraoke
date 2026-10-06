import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Story } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { ResonateRow } from '@/features/resonance/ResonateRow';
import { formatDuration, timeAgo } from '@/lib/format';
import { colors, radius, space, type } from '@/theme';

type Props = { story: Story; onDelete?: () => void };

export function StoryCard({ story, onDelete }: Props) {
  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <Avatar name={story.author.displayName} />
        <View style={styles.who}>
          <Text style={styles.name} numberOfLines={1}>{story.author.displayName}</Text>
          <Text style={styles.meta} numberOfLines={1}>@{story.author.handle} · {timeAgo(story.createdAt)}</Text>
        </View>
        {story.mine && onDelete ? (
          <Pressable accessibilityRole="button" accessibilityLabel="Delete story" onPress={onDelete} hitSlop={12}>
            <Text style={styles.more}>•••</Text>
          </Pressable>
        ) : null}
      </View>

      <Text style={styles.body}>{story.body}</Text>

      <StoryChips momentSec={story.momentSec} year={story.yearOfMemory} />

      <ResonateRow story={story} />
    </View>
  );
}

/** "at 2:14" and "2009" tags. These are what make a story feel anchored in the song and in time. */
function StoryChips({ momentSec, year }: { momentSec: number | null; year: number | null }) {
  if (momentSec == null && year == null) return null;
  return (
    <View style={styles.chips}>
      {momentSec != null ? <Chip label={`▶ at ${formatDuration(momentSec)}`} /> : null}
      {year != null ? <Chip label={`☾ ${year}`} /> : null}
    </View>
  );
}

function Chip({ label }: { label: string }) {
  return (
    <View style={styles.chip}>
      <Text style={styles.chipText}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: space.sm,
    padding: space.md,
    backgroundColor: colors.surface,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  header: { flexDirection: 'row', alignItems: 'center', gap: space.sm },
  who: { flex: 1 },
  name: { ...type.body, fontSize: 15, fontWeight: '600' },
  meta: { ...type.hint, color: colors.textMuted },
  more: { color: colors.textMuted, fontSize: 14, letterSpacing: 1 },
  body: { ...type.body, lineHeight: 23 },
  chips: { flexDirection: 'row', gap: space.sm },
  chip: { paddingHorizontal: space.sm, paddingVertical: 3, borderRadius: 999, borderWidth: 1, borderColor: colors.accent },
  chipText: { ...type.hint, color: colors.accent, fontWeight: '600' },
});
