import { Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Story } from '@/api/client';
import { Avatar } from '@/components/Avatar';
import { ResonateRow } from '@/features/resonance/ResonateRow';
import { openStoryMenu } from './storyMenu';
import { timeAgo } from '@/lib/format';
import { makeStyles, paperOf, radius, space } from '@/theme';
import { StoryPaper } from './StoryPaper';

// Feeds show a preview; the story page shows everything.
const PREVIEW_LINES = 8;
const PREVIEW_CHARS = 450;

type Props = {
  story: Story;
  onDeleted?: (id: number) => void;
  /** Show the whole story (story page). Feeds leave this off and collapse long ones. */
  expanded?: boolean;
};

export function StoryCard({ story, onDeleted, expanded = false }: Props) {
  const styles = useStyles();
  const openAuthor = () => router.push({ pathname: '/user/[handle]', params: { handle: story.author.handle } });

  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <Pressable accessibilityRole="link" onPress={openAuthor} style={styles.author}>
          <Avatar name={story.author.displayName} url={story.author.avatarUrl} />
          <View style={styles.who}>
            <Text style={styles.name} numberOfLines={1}>{story.author.displayName}</Text>
            <Text style={styles.meta} numberOfLines={1}>
              @{story.author.handle} · {timeAgo(story.createdAt)}{story.editedAt ? ' · edited' : ''}
            </Text>
          </View>
        </Pressable>
        {story.mine ? (
          <Pressable accessibilityRole="button" accessibilityLabel="Story options" onPress={() => openStoryMenu(story, onDeleted)} hitSlop={12}>
            <Text style={styles.more}>•••</Text>
          </Pressable>
        ) : null}
      </View>

      <StoryBody story={story} expanded={expanded} />

      <ResonateRow story={story} />
    </View>
  );
}

/**
 * The story printed on its paper. Long stories collapse to a preview with "Read more",
 * which opens the full story page.
 */
function StoryBody({ story, expanded }: { story: Story; expanded: boolean }) {
  const styles = useStyles();
  const isLong = story.body.length > PREVIEW_CHARS || story.body.split('\n').length > PREVIEW_LINES;
  const collapsed = !expanded && isLong;
  const paper = (
    <StoryPaper
      paper={story.paper}
      body={story.body}
      lyric={story.lyricQuote}
      momentSec={story.momentSec}
      year={story.yearOfMemory}
      bodyLines={collapsed ? PREVIEW_LINES : undefined}>
      {collapsed ? <Text style={[styles.readMore, { color: paperOf(story.paper).muted }]}>Read more ›</Text> : null}
    </StoryPaper>
  );
  if (!collapsed) return paper;

  const openStory = () => router.push({ pathname: '/story/[id]', params: { id: String(story.id) } });
  return (
    <Pressable accessibilityRole="link" accessibilityHint="Opens the full story" onPress={openStory}>
      {paper}
    </Pressable>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  card: {
    gap: space.sm,
    padding: space.md,
    backgroundColor: colors.surface,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  header: { flexDirection: 'row', alignItems: 'center', gap: space.sm },
  author: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: space.sm },
  who: { flex: 1 },
  name: { ...type.body, fontSize: 15, fontWeight: '600' },
  meta: { ...type.hint, color: colors.textMuted },
  more: { color: colors.textMuted, fontSize: 14, letterSpacing: 1 },
  readMore: { ...type.hint, fontWeight: '600' },
}));
