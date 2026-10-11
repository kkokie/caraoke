import { useCallback, useEffect, useState } from 'react';
import { Alert } from 'react-native';
import { router } from 'expo-router';
import {
  api, PageBackgroundName, PageBlockName, PageThemeName, PageTypeName, PageView, SongSummary, StoryTile,
} from '@/api/client';

export const ALL_BLOCKS: PageBlockName[] = ['PAGE_SONG', 'PINNED_STORY', 'LIFE_IN_SONGS', 'RAISED_ME'];
export const MAX_RAISED = 5;

export type PageDraft = {
  theme: PageThemeName;
  background: PageBackgroundName;
  type: PageTypeName;
  order: PageBlockName[];             // every block, in the order you arranged them
  shown: PageBlockName[];             // the ones that appear
  pageSong: SongSummary | null;
  pinned: StoryTile | null;
  raised: SongSummary[];
};

function draftOf(p: PageView): PageDraft {
  return {
    theme: p.theme,
    background: p.background,
    type: p.type,
    order: [...p.blocks, ...ALL_BLOCKS.filter((b) => !p.blocks.includes(b))],
    shown: p.blocks,
    pageSong: p.pageSong,
    pinned: p.pinnedStory,
    raised: p.raisedMe,
  };
}

/** All state for decorating your page; the screen stays purely visual. */
export function usePageEditor(handle: string) {
  const [draft, setDraft] = useState<PageDraft | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.getPage(handle).then((p) => setDraft(draftOf(p))).catch((e) => setError(e instanceof Error ? e.message : 'Couldn’t load'));
  }, [handle]);

  const change = useCallback((patch: Partial<PageDraft>) => setDraft((d) => (d ? { ...d, ...patch } : d)), []);

  const toggleBlock = (b: PageBlockName) => setDraft((d) => d && ({
    ...d,
    shown: d.shown.includes(b) ? d.shown.filter((x) => x !== b) : d.order.filter((x) => x === b || d.shown.includes(x)),
  }));

  const moveBlock = (b: PageBlockName, by: -1 | 1) => setDraft((d) => {
    if (!d) return d;
    const i = d.order.indexOf(b), j = i + by;
    if (j < 0 || j >= d.order.length) return d;
    const order = [...d.order];
    [order[i], order[j]] = [order[j], order[i]];
    return { ...d, order, shown: order.filter((x) => d.shown.includes(x)) };
  });

  const addRaised = (s: SongSummary) => setDraft((d) => d && d.raised.length < MAX_RAISED && !d.raised.some((r) => r.id === s.id)
    ? { ...d, raised: [...d.raised, s] } : d);

  const removeRaised = (id: number) => setDraft((d) => d && ({ ...d, raised: d.raised.filter((r) => r.id !== id) }));

  async function save() {
    if (!draft || saving) return;
    setSaving(true);
    try {
      await api.savePage({
        theme: draft.theme,
        background: draft.background,
        type: draft.type,
        blocks: draft.shown,
        pageSongId: draft.pageSong?.id ?? null,
        pinnedStoryId: draft.pinned?.storyId ?? null,
        raisedSongIds: draft.raised.map((r) => r.id),
      });
      router.back();
    } catch (e) {
      Alert.alert('Couldn’t save your page', e instanceof Error ? e.message : String(e));
    } finally {
      setSaving(false);
    }
  }

  return { draft, error, saving, change, toggleBlock, moveBlock, addRaised, removeRaised, save };
}
