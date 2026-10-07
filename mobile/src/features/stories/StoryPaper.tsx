import { ReactNode } from 'react';
import { Text, View } from 'react-native';
import { PaperName } from '@/api/client';
import { formatDuration } from '@/lib/format';
import { fonts, makeStyles, paperOf, radius, space } from '@/theme';

type Props = {
  paper: PaperName;
  body: string;
  lyric?: string | null;
  momentSec?: number | null;
  year?: number | null;
  /** Feeds collapse long stories; the story page and the share preview don't. */
  bodyLines?: number;
  children?: ReactNode;   // e.g. a "Read more" link, drawn in the paper's colors by the caller
};

/**
 * The story itself, printed on the paper its author picked: the line they felt (lyric font),
 * the story (serif), and the moment + year in mono. Paper colors are the same in light and dark mode.
 */
export function StoryPaper({ paper, body, lyric, momentSec, year, bodyLines, children }: Props) {
  const styles = useStyles();
  const p = paperOf(paper);
  const meta = [momentSec != null ? `▶ ${formatDuration(momentSec)}` : null, year != null ? String(year) : null]
    .filter(Boolean)
    .join('  ·  ');

  return (
    <View style={[styles.sheet, { backgroundColor: p.bg, borderColor: p.border }]}>
      {lyric ? <Text style={[styles.lyric, { color: p.text }]}>“{lyric}”</Text> : null}
      <Text style={[styles.body, { color: p.text }]} numberOfLines={bodyLines}>{body}</Text>
      {children}
      {meta ? <Text style={[styles.meta, { color: p.muted }]}>{meta}</Text> : null}
    </View>
  );
}

const useStyles = makeStyles(({ type }) => ({
  sheet: { gap: space.sm, padding: space.md, borderRadius: radius.md, borderWidth: 1 },
  lyric: { ...type.lyric },
  body: { ...type.story },
  meta: { fontFamily: fonts.mono, fontSize: 12, letterSpacing: 1 },
}));
