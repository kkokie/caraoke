import { useState } from 'react';
import { router } from 'expo-router';
import { api, Song } from '@/api/client';
import { formatDuration, parseDuration } from '@/lib/format';

export const MAX_BODY = 2000;
const MIN_YEAR = 1900;

/** Mirrors backend StoryRules so problems show up while typing, not after tapping Post. */
function validateMoment(text: string, durationSec: number | null): { value?: number; error?: string } {
  if (!text.trim()) return {};
  const sec = parseDuration(text);
  if (sec == null) return { error: 'Use minutes:seconds, like 2:14' };
  if (durationSec != null && sec > durationSec) return { error: `The song is only ${formatDuration(durationSec)}` };
  return { value: sec };
}

function validateYear(text: string): { value?: number; error?: string } {
  if (!text.trim()) return {};
  const year = Number(text);
  const thisYear = new Date().getFullYear();
  if (!/^\d{4}$/.test(text.trim()) || year < MIN_YEAR || year > thisYear) {
    return { error: `Pick a year between ${MIN_YEAR} and ${thisYear}` };
  }
  return { value: year };
}

/** All composer state + submit; the screen just renders it. */
export function useComposeStory(song: Song) {
  const [body, setBody] = useState('');
  const [momentText, setMomentText] = useState('');
  const [yearText, setYearText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const moment = validateMoment(momentText, song.durationSec);
  const year = validateYear(yearText);
  const canSubmit = body.trim().length > 0 && !moment.error && !year.error && !submitting;

  async function submit() {
    if (!canSubmit) return;
    setSubmitting(true);
    setSubmitError(null);
    try {
      await api.postStory(song.id, { body: body.trim(), momentSec: moment.value, yearOfMemory: year.value });
      router.back();   // the song page refreshes its feed on focus
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : 'Couldn’t post your story');
      setSubmitting(false);
    }
  }

  return {
    body, setBody: (v: string) => setBody(v.slice(0, MAX_BODY)),
    momentText, setMomentText, momentError: moment.error,
    yearText, setYearText, yearError: year.error,
    canSubmit, submitting, submitError, submit,
  };
}
