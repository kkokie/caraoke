import { Text, View } from 'react-native';
import { LoadingState } from '@/components/LoadingState';
import { Button } from '@/components/Button';
import { Screen } from '@/components/Screen';
import { BlockOrder } from '@/features/page/editor/BlockOrder';
import { OptionChips } from '@/features/page/editor/OptionChips';
import { PageSongSection } from '@/features/page/editor/PageSongSection';
import { PinPicker } from '@/features/page/editor/PinPicker';
import { RaisedSongsSection } from '@/features/page/editor/RaisedSongsSection';
import { usePageEditor } from '@/features/page/editor/usePageEditor';
import { useSession } from '@/features/session/useSession';
import { makeStyles, PAGE_THEMES, PAGE_TYPES, pagePalette, space, useTheme } from '@/theme';

const BACKGROUNDS = [
  { name: 'PLAIN', label: 'Plain' }, { name: 'DOTS', label: 'Dots' }, { name: 'LINES', label: 'Lines' },
  { name: 'GRID', label: 'Grid' }, { name: 'STARS', label: 'Stars' },
] as const;

/** Decorate your page: look, page song, pinned story, songs that raised you, sections. */
export default function DecoratePageScreen() {
  const session = useSession();
  if (session.status !== 'ready') return null;
  return <Editor handle={session.profile.handle} />;
}

function Editor({ handle }: { handle: string }) {
  const styles = useStyles();
  const { scheme } = useTheme();
  const e = usePageEditor(handle);
  if (!e.draft) return <LoadingState error={e.error} />;
  const d = e.draft;
  const themes = PAGE_THEMES.map((t) => ({ ...t, swatch: pagePalette(t.name, scheme).accent }));

  return (
    <Screen>
      <Text style={styles.intro}>Make it yours. Visitors see your page in these colors, in light or dark to match their phone.</Text>
      <OptionChips label="THEME" options={themes} value={d.theme} onChange={(theme) => e.change({ theme })} />
      <OptionChips label="BACKGROUND" options={[...BACKGROUNDS]} value={d.background} onChange={(background) => e.change({ background })} />
      <OptionChips label="TYPE" options={PAGE_TYPES} value={d.type} onChange={(type) => e.change({ type })} />

      <Divider />
      <PageSongSection song={d.pageSong} onChange={(pageSong) => e.change({ pageSong })} />
      <Divider />
      <PinPicker handle={handle} pinned={d.pinned} onChange={(pinned) => e.change({ pinned })} />
      <Divider />
      <RaisedSongsSection songs={d.raised} onAdd={e.addRaised} onRemove={e.removeRaised} />
      <Divider />
      <BlockOrder order={d.order} shown={d.shown} onToggle={e.toggleBlock} onMove={e.moveBlock} />

      <Button label="Save page" onPress={e.save} loading={e.saving} />
    </Screen>
  );
}

function Divider() {
  const styles = useStyles();
  return <View style={styles.divider} />;
}

const useStyles = makeStyles(({ colors, type }) => ({
  intro: { ...type.subtitle, marginTop: space.sm },
  divider: { height: 1, backgroundColor: colors.border, marginVertical: space.xs },
}));
