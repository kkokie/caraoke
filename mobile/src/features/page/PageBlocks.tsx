import { View } from 'react-native';
import { PageBlockName, PageView } from '@/api/client';
import { space } from '@/theme';
import { LifeInSongsBlock } from './blocks/LifeInSongsBlock';
import { PageSongBlock } from './blocks/PageSongBlock';
import { PinnedStoryBlock } from './blocks/PinnedStoryBlock';
import { RaisedMeBlock } from './blocks/RaisedMeBlock';

/** The blocks they chose, in their order. A block with nothing in it is simply skipped. */
export function PageBlocks({ page }: { page: PageView }) {
  const shown = page.blocks.map((b) => render(b, page)).filter(Boolean);
  if (shown.length === 0) return null;
  return <View style={{ gap: space.lg, paddingVertical: space.md }}>{shown}</View>;
}

function render(block: PageBlockName, page: PageView) {
  switch (block) {
    case 'PAGE_SONG':
      return page.pageSong ? <PageSongBlock key={block} song={page.pageSong} /> : null;
    case 'PINNED_STORY':
      return page.pinnedStory ? <PinnedStoryBlock key={block} story={page.pinnedStory} /> : null;
    case 'LIFE_IN_SONGS':
      return page.lifeInSongs.length ? <LifeInSongsBlock key={block} stops={page.lifeInSongs} /> : null;
    case 'RAISED_ME':
      return page.raisedMe.length ? <RaisedMeBlock key={block} songs={page.raisedMe} /> : null;
  }
}
