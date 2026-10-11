import { Text, View } from 'react-native';
import { ModerationAction, QueueItem } from '@/api/client';
import { Button } from '@/components/Button';
import { REPORT_REASONS } from '@/features/safety/reasons';
import { timeAgo } from '@/lib/format';
import { makeStyles, radius, space } from '@/theme';

const reasonLabel = (r: string) => REPORT_REASONS.find((x) => x.reason === r)?.label ?? r;

/** One reported story or person, with what people said and the three decisions. */
export function QueueCard({ item, onDecide }: { item: QueueItem; onDecide: (a: ModerationAction) => void }) {
  const styles = useStyles();
  const gone = item.kind === 'STORY' && !item.story;

  return (
    <View style={styles.card}>
      <Text style={styles.label}>
        {item.kind === 'STORY' ? 'STORY' : 'PERSON'} · {item.reporters} {item.reporters === 1 ? 'REPORTER' : 'REPORTERS'} · {timeAgo(item.lastReportedAt)}
      </Text>

      {item.person ? <PersonLine person={item.person} /> : null}
      {item.story ? <StoryExcerpt story={item.story} /> : null}
      {gone ? <Text style={styles.muted}>The author deleted this story.</Text> : null}

      <View style={styles.reports}>
        {item.reports.map((r, i) => (
          <Text key={i} style={styles.report}>
            <Text style={styles.reason}>{reasonLabel(r.reason)}</Text>{r.details ? `: “${r.details}”` : ''}
          </Text>
        ))}
      </View>

      <View style={styles.actions}>
        <View style={styles.action}><Button label="Dismiss" variant="ghost" onPress={() => onDecide('DISMISS')} /></View>
        {!gone ? <View style={styles.action}><Button label="Strike" variant="ghost" onPress={() => onDecide('STRIKE')} /></View> : null}
        {!gone ? <View style={styles.action}><Button label="Ban" onPress={() => onDecide('BAN')} /></View> : null}
      </View>
    </View>
  );
}

function PersonLine({ person }: { person: NonNullable<QueueItem['person']> }) {
  const styles = useStyles();
  const standing = person.standing === 'GOOD' ? 'good standing' : person.standing.toLowerCase();
  return (
    <Text style={styles.person}>
      {person.displayName} <Text style={styles.muted}>@{person.handle} · {standing} · {person.activeStrikes} {person.activeStrikes === 1 ? 'strike' : 'strikes'}</Text>
    </Text>
  );
}

function StoryExcerpt({ story }: { story: NonNullable<QueueItem['story']> }) {
  const styles = useStyles();
  return (
    <View style={styles.story}>
      <Text style={styles.label}>{story.songTitle} — {story.songArtist}{story.status !== 'visible' ? ` · ${story.status.toUpperCase()}` : ''}</Text>
      {story.lyricQuote ? <Text style={styles.lyric}>“{story.lyricQuote}”</Text> : null}
      <Text style={styles.body} numberOfLines={12}>{story.body}</Text>
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  card: { gap: space.sm, padding: space.md, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.surface },
  label: { ...type.label },
  person: { ...type.body, fontWeight: '600' },
  muted: { ...type.hint, fontWeight: '400' },
  story: { gap: space.xs, paddingVertical: space.xs },
  lyric: { ...type.lyric, fontSize: 17, lineHeight: 22 },
  body: { ...type.story, fontSize: 16, lineHeight: 22 },
  reports: { gap: space.xs },
  report: { ...type.hint },
  reason: { ...type.hint, color: colors.text, fontWeight: '600' },
  actions: { flexDirection: 'row', gap: space.sm, paddingTop: space.xs },
  action: { flex: 1 },
}));
