import { useState } from 'react';
import { router } from 'expo-router';
import { Alert } from 'react-native';
import { api, ApiError, LocalImage, PaperName, Song, Story, StoryPhoto } from '@/api/client';
import { pickStoryPhoto } from './pickStoryPhoto';

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
  // Cover: the album art, the photo it already has, or a new one picked here (uploaded on Share)
  const [savedPhoto, setSavedPhoto] = useState<StoryPhoto | null>(existing?.photo ?? null);
  const [newPhoto, setNewPhoto] = useState<LocalImage | null>(null);
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
      const story = existing ? await api.editStory(existing.id, payload) : await api.postStory(song.id, payload);
      await syncPhoto(story.id);
      leave();
    } catch (e) {
      setError(messageFor(e));
      setSubmitting(false);
    }
  }

  /** After the words are saved: upload a new photo, or drop the old one if you went back to album art. */
  async function syncPhoto(storyId: number) {
    try {
      if (newPhoto) await api.uploadStoryPhoto(storyId, newPhoto);
      else if (existing?.photo && !savedPhoto) await api.removeStoryPhoto(storyId);
    } catch (e) {
      // The story itself is saved; say so plainly rather than failing the whole share
      Alert.alert('Your story is shared, but the photo didn’t upload', e instanceof Error ? e.message : String(e));
    }
  }

  async function choosePhoto() {
    try {
      const picked = await pickStoryPhoto();
      if (picked) setNewPhoto(picked);
    } catch (e) {
      Alert.alert('Couldn’t open your photos', e instanceof Error ? e.message : String(e));
    }
  }

  function chooseAlbumArt() {
    setNewPhoto(null);
    setSavedPhoto(null);
  }

  const hasPhoto = !!newPhoto || !!savedPhoto;

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
    photo: { local: newPhoto, saved: savedPhoto, hasPhoto, choose: choosePhoto, chooseAlbumArt },
    canContinue, next, back, submit, submitting, error,
  };
}

export type ShareDraft = ReturnType<typeof useShareDraft>;

function messageFor(e: unknown): string {
  if (e instanceof ApiError && e.status === 429) return e.message;   // "You've shared your story for today…"
  return e instanceof Error ? e.message : 'Couldn’t share your story';
}
