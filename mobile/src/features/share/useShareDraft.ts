import { useState } from 'react';
import { router } from 'expo-router';
import { api, ApiError, PaperName, Song, Story } from '@/api/client';

// Mirrors backend StoryRules so problems show while writing, not after tapping Share.
export const MAX_BODY = 10_000;
export const MAX_LYRIC = 120;
export const MIN_YEAR = 1900;

export type ShareStep = 'moment' | 'words' | 'look';
export const STEPS: ShareStep[] = ['moment', 'words', 'look'];

/** Where the composer was opened from decides where you land after sharing. */
export type ShareOrigin = 'song' | 'plus';

/**
 * Everything the 3-step share flow edits, plus submit. Screens only render it.
 * Pass `existing` to edit a story: fields start filled in and Share saves changes instead.
 */
export function useShareDraft(song: Song, existing?: Story, origin: ShareOrigin = 'song') {
  const [step, setStep] = useState<ShareStep>('moment');
  const [momentSec, setMomentSec] = useState<number | null>(existing?.momentSec ?? null);
  const [year, setYear] = useState<number | null>(existing?.yearOfMemory ?? null);
  const [lyric, setLyric] = useState(existing?.lyricQuote ?? '');
  const [body, setBody] = useState(existing?.body ?? '');
  const [paper, setPaper] = useState<PaperName>(existing?.paper ?? 'cream');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const index = STEPS.indexOf(step);
  const canContinue = step !== 'words' || body.trim().length > 0;

  function next() {
    if (canContinue && index < STEPS.length - 1) setStep(STEPS[index + 1]);
  }

  /** Back one step; from the first step, leave the composer. */
  function back() {
    if (index > 0) setStep(STEPS[index - 1]);
    else router.back();
  }

  async function submit() {
    if (submitting || !body.trim()) return;
    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        body: body.trim(),
        momentSec: momentSec ?? undefined,
        yearOfMemory: year ?? undefined,
        lyricQuote: lyric.trim() || undefined,
        paper,
      };
      if (existing) await api.editStory(existing.id, payload);
      else await api.postStory(song.id, payload);
      leave();
    } catch (e) {
      setError(messageFor(e));
      setSubmitting(false);
    }
  }

  // From the song page: go back to it (it refreshes on focus). From the + button there's no
  // song page behind us, so open it in place of the composer.
  function leave() {
    if (origin === 'plus') router.replace({ pathname: '/song/[id]', params: { id: String(song.id) } });
    else router.back();
  }

  return {
    song, step, index, isEdit: !!existing,
    momentSec, setMomentSec, year, setYear,
    lyric, setLyric: (v: string) => setLyric(v.slice(0, MAX_LYRIC)),
    body, setBody: (v: string) => setBody(v.slice(0, MAX_BODY)),
    paper, setPaper,
    canContinue, next, back, submit, submitting, error,
  };
}

export type ShareDraft = ReturnType<typeof useShareDraft>;

function messageFor(e: unknown): string {
  if (e instanceof ApiError && e.status === 429) return e.message;   // "You've shared your story for today…"
  return e instanceof Error ? e.message : 'Couldn’t share your story';
}
